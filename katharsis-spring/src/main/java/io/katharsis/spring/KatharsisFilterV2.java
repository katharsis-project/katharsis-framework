package io.katharsis.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;
import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.errorhandling.exception.KatharsisMappableException;
import io.katharsis.errorhandling.exception.KatharsisMatchingException;
import io.katharsis.errorhandling.mapper.KatharsisExceptionMapper;
import io.katharsis.invoker.JsonApiMediaType;
import io.katharsis.invoker.KatharsisInvokerException;
import io.katharsis.jackson.exception.JsonDeserializationException;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.servlet.util.BufferedRequestWrapper;
import io.katharsis.spring.boot.KatharsisSpringBootProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.http.HttpHeaders;

import javax.annotation.Priority;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

@Priority(20)
public class KatharsisFilterV2 implements Filter, BeanFactoryAware {

    private static final int BUFFER_SIZE = 4096;

    private ObjectMapper objectMapper;
    private ResourceRegistry resourceRegistry;
    private RequestDispatcher requestDispatcher;
    private KatharsisSpringBootProperties properties;

    private ConfigurableBeanFactory beanFactory;


    public KatharsisFilterV2(ObjectMapper objectMapper,
                             ResourceRegistry resourceRegistry,
                             RequestDispatcher requestDispatcher, KatharsisSpringBootProperties properties) {
        this.objectMapper = objectMapper;
        this.resourceRegistry = resourceRegistry;
        this.requestDispatcher = requestDispatcher;
        this.properties = properties;



    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.beanFactory = (ConfigurableBeanFactory) beanFactory;
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (req instanceof HttpServletRequest && res instanceof HttpServletResponse) {
            HttpServletRequest request = new BufferedRequestWrapper((HttpServletRequest) req);
            HttpServletResponse response = (HttpServletResponse) res;
            req.setCharacterEncoding("UTF-8");

            boolean passToFilters = invoke(request, response);
            if (passToFilters) {
                chain.doFilter(request, res);
            }
        } else {
            chain.doFilter(req, res);
        }
    }

    private boolean invoke(HttpServletRequest request, HttpServletResponse response) {
        if (!isAcceptablePath(request)) {
            return true;
        }
        if (isAcceptableMediaType(request)) {
            try {
                return dispatchRequest(request, response);
            } catch (Exception e) {
                throw new KatharsisInvokerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
            }
        } else {
            return true;
        }
    }

    private boolean dispatchRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        BaseResponseContext katharsisResponse = null;

        boolean passToFilters = false;

        InputStream in = null;

        try {
            JsonPath jsonPath = new PathBuilder(resourceRegistry).buildPath(getRequestPath(request));

            Map<String, Set<String>> parameters = getParameters(request);

            in = request.getInputStream();
            RequestBody requestBody = inputStreamToBody(in);

            String method = request.getMethod();
            RepositoryMethodParameterProvider parameterProvider = new SpringParameterProvider(beanFactory, request);
            katharsisResponse = requestDispatcher.dispatchRequest(jsonPath, method, parameters, parameterProvider,
                    requestBody);
        } catch (KatharsisMappableException e) {
            // log error in KatharsisMappableException mapper.
            katharsisResponse = new KatharsisExceptionMapper().toErrorResponse(e);
        } catch (KatharsisMatchingException e) {
            passToFilters = true;
        } finally {
            if (!passToFilters) {
                closeQuietly(in);

                if (katharsisResponse != null) {
                    response.setStatus(katharsisResponse.getHttpStatus());
                    response.setContentType(JsonApiMediaType.APPLICATION_JSON_API);

                    addCORSHeaders(response);

                    ByteArrayOutputStream baos = null;
                    OutputStream out = null;

                    try {
                        // first write to a buffer first because objectMapper may fail while writing.
                        baos = new ByteArrayOutputStream(BUFFER_SIZE);
                        objectMapper.writeValue(baos, katharsisResponse);

                        out = response.getOutputStream();
                        out.write(baos.toByteArray());
                        out.flush();
                    } finally {
                        closeQuietly(baos);
                        closeQuietly(out);
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }
            }
        }
        return passToFilters;
    }

    private void addCORSHeaders(HttpServletResponse response) {
        if (properties != null) {
            if (properties.getAllowOrigin() != null) {
                response.addHeader("Access-Control-Allow-Origin", properties.getAllowOrigin());
            }
            if (properties.getAllowMethods() != null) {
                response.addHeader("Access-Control-Allow-Methods", properties.getAllowMethods());
            }
            if (properties.getAllowHeaders() != null) {
                response.addHeader("Access-Control-Allow-Headers", properties.getAllowHeaders());
            }
            if (properties.getMaxAge() != null) {
                response.addHeader("Access-Control-Max-Age", properties.getMaxAge().toString());
            }
        }
    }

    private String getPathPrefix() {
        if (properties != null) {
            return properties.getPathPrefix() != null ? properties.getPathPrefix() : "";
        } else {
            return "";
        }
    }

    private boolean isAcceptablePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();

        if (contextPath.startsWith("/") && contextPath.length() == 1) {
            contextPath = "";
        }
        return request.getRequestURI().startsWith(contextPath + getPathPrefix());
    }

    private String getRequestPath(HttpServletRequest request) {
        String path = request.getPathInfo();

        // Serving with Filter, pathInfo can be null.
        if (path == null) {
            path = request.getRequestURI()
                    .substring(request.getContextPath().length() + getPathPrefix().length());
        }

        return path;
    }

    private boolean isAcceptableMediaType(HttpServletRequest servletRequest) {
        String acceptHeader = servletRequest.getHeader(HttpHeaders.ACCEPT);

        if (acceptHeader != null) {
            String[] accepts = acceptHeader.split(",");
            MediaType acceptableType;

            for (String mediaTypeItem : accepts) {
                try {
                    acceptableType = MediaType.parse(mediaTypeItem.trim());

                    if (JsonApiMediaType.isCompatibleMediaType(acceptableType)) {
                        return true;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }

        return false;
    }

    /**
     * This method return a list o parameters. It uses {@link ServletRequest#getParameterMap()} which can also return POST
     * body parameters, but we don't expect to receive such body.
     *
     * @param request request body
     * @path
     * @return query parameters
     */
    private Map<String, Set<String>> getParameters(HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();

        Map<String, Set<String>> queryParameters = new HashMap<>(params.size());
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            queryParameters.put(entry.getKey(), new HashSet<>(Arrays.asList(entry.getValue())));
        }
        return queryParameters;
    }

    private RequestBody inputStreamToBody(InputStream is) {
        if (is == null) {
            return null;
        }

        Scanner s = new Scanner(is).useDelimiter("\\A");
        String requestBody = s.hasNext() ? s.next() : "";

        if (requestBody == null || requestBody.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readValue(requestBody, RequestBody.class);
        } catch (IOException e) {
            throw new JsonDeserializationException(e.getMessage());
        }
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignore) {
            }
        }
    }

    @Override
    public void destroy() {

    }
}


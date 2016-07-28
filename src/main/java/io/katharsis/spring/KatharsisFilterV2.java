package io.katharsis.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;
import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.errorhandling.exception.KatharsisMappableException;
import io.katharsis.errorhandling.exception.KatharsisMatchingException;
import io.katharsis.errorhandling.mapper.KatharsisExceptionMapper;
import io.katharsis.invoker.JsonApiMediaType;
import io.katharsis.invoker.KatharsisInvokerException;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.servlet.util.BufferedRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.http.HttpHeaders;

import javax.annotation.Priority;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@Priority(20)
public class KatharsisFilterV2 implements Filter, BeanFactoryAware {

    private static final Logger log = LoggerFactory.getLogger(KatharsisFilterV2.class);

    private static final int BUFFER_SIZE = 4096;

    private ObjectMapper objectMapper;
    private QueryParamsBuilder queryParamsBuilder;
    private ResourceRegistry resourceRegistry;
    private RequestDispatcher requestDispatcher;
    private String webPathPrefix;

    private ConfigurableBeanFactory beanFactory;


    public KatharsisFilterV2(ObjectMapper objectMapper,
                             QueryParamsBuilder queryParamsBuilder,
                             ResourceRegistry resourceRegistry,
                             RequestDispatcher requestDispatcher, String webPathPrefix) {
        this.objectMapper = objectMapper;
        this.queryParamsBuilder = queryParamsBuilder;
        this.resourceRegistry = resourceRegistry;
        this.requestDispatcher = requestDispatcher;
        this.webPathPrefix = webPathPrefix != null ? webPathPrefix : "";
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
                chain.doFilter(req, res);
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

            QueryParams queryParams = createQueryParams(request);

            in = request.getInputStream();
            RequestBody requestBody = inputStreamToBody(in);

            String method = request.getMethod();
            RepositoryMethodParameterProvider parameterProvider = new SpringParameterProvider(beanFactory, request);
            katharsisResponse = requestDispatcher.dispatchRequest(jsonPath, method, queryParams, parameterProvider,
                requestBody);
        } catch (KatharsisMappableException e) {
            if (log.isDebugEnabled()) {
                log.warn("Error occurred while dispatching katharsis request. " + e, e);
            } else {
                log.warn("Error occurred while dispatching katharsis request. " + e);
            }
            katharsisResponse = new KatharsisExceptionMapper().toErrorResponse(e);
        } catch (KatharsisMatchingException e) {
            passToFilters = true;
        } finally {
            if (!passToFilters) {
                closeQuietly(in);

                if (katharsisResponse != null) {
                    response.setStatus(katharsisResponse.getHttpStatus());
                    response.setContentType(JsonApiMediaType.APPLICATION_JSON_API);

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

    private boolean isAcceptablePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        if (contextPath.startsWith("/") && contextPath.length() == 1) {
            contextPath = "";
        }
        return request.getRequestURI().startsWith(contextPath + webPathPrefix);
    }

    private String getRequestPath(HttpServletRequest request) {
        String path = request.getPathInfo();

        // Serving with Filter, pathInfo can be null.
        if (path == null) {
            path = request.getRequestURI()
                .substring(request.getContextPath().length() + webPathPrefix.length());
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
     * @return query parameters
     */
    private QueryParams createQueryParams(HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();

        Map<String, Set<String>> queryParameters = new HashMap<>(params.size());
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            queryParameters.put(entry.getKey(), new HashSet<>(Arrays.asList(entry.getValue())));
        }
        return queryParamsBuilder.buildQueryParams(queryParameters);
    }

    private RequestBody inputStreamToBody(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }

        Scanner s = new Scanner(is).useDelimiter("\\A");
        String requestBody = s.hasNext() ? s.next() : "";

        if (requestBody == null || requestBody.isEmpty()) {
            return null;
        }

        return objectMapper.readValue(requestBody, RequestBody.class);
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


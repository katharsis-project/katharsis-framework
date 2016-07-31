package io.katharsis.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;
import io.katharsis.dispatcher.JsonApiDispatcher;
import io.katharsis.dispatcher.ResponseContext;
import io.katharsis.dispatcher.registry.api.RepositoryRegistry;
import io.katharsis.invoker.JsonApiMediaType;
import io.katharsis.invoker.KatharsisInvokerException;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.Request;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.servlet.util.BufferedRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Priority(20)
public class KatharsisFilterV2 implements Filter, BeanFactoryAware {

    private static final Logger log = LoggerFactory.getLogger(KatharsisFilterV2.class);

    private static final int BUFFER_SIZE = 4096;

    private ObjectMapper objectMapper;
    private RepositoryRegistry resourceRegistry;
    private JsonApiDispatcher requestDispatcher;
    private String webPathPrefix;

    private ConfigurableBeanFactory beanFactory;


    public KatharsisFilterV2(ObjectMapper objectMapper,
                             RepositoryRegistry resourceRegistry,
                             JsonApiDispatcher requestDispatcher, String webPathPrefix) {
        this.objectMapper = objectMapper;
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

    private boolean dispatchRequest(HttpServletRequest req, HttpServletResponse res) throws Exception {
        ResponseContext katharsisResponse = null;

        boolean passToFilters = false;

        InputStream in = null;

        try {

            String httpMethod = req.getMethod();

            RepositoryMethodParameterProvider parameterProvider = new SpringParameterProvider(beanFactory, req);

            JsonApiPath path = JsonApiPath.parsePathFromStringUrl(req.getRequestURI(), webPathPrefix);

            in = req.getInputStream();
            Request request = new Request(path, httpMethod, in, parameterProvider);

            katharsisResponse = requestDispatcher.handle(request);

        } catch (Exception e) {
            //TODO: ieugen: handle exception
            log.error("Exception {}", e);
        } finally {
            if (!passToFilters) {
                closeQuietly(in);

                if (katharsisResponse != null) {
                    res.setStatus(katharsisResponse.getHttpStatus());
                    res.setContentType(JsonApiMediaType.APPLICATION_JSON_API);

                    ByteArrayOutputStream baos = null;
                    OutputStream out = null;

                    try {
                        // first write to a buffer first because objectMapper may fail while writing.
                        baos = new ByteArrayOutputStream(BUFFER_SIZE);
                        objectMapper.writeValue(baos, katharsisResponse);

                        out = res.getOutputStream();
                        out.write(baos.toByteArray());
                        out.flush();
                    } finally {
                        closeQuietly(baos);
                        closeQuietly(out);
                    }
                } else {
                    res.setStatus(HttpServletResponse.SC_NO_CONTENT);
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


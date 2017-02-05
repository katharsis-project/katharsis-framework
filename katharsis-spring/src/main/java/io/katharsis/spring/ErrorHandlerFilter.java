package io.katharsis.spring;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Priority;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.internal.exception.ExceptionMapperRegistry;
import io.katharsis.errorhandling.ErrorResponse;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;
import io.katharsis.invoker.internal.KatharsisInvokerException;
import io.katharsis.utils.Optional;

@Priority(10)
public class ErrorHandlerFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandlerFilter.class);

    private ObjectMapper objectMapper;
    private ExceptionMapperRegistry exceptionMapperRegistry;

    public ErrorHandlerFilter(ObjectMapper objectMapper, ExceptionMapperRegistry exceptionMapperRegistry) {
        this.objectMapper = objectMapper;
        this.exceptionMapperRegistry = exceptionMapperRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (KatharsisInvokerException e) {
            Optional<JsonApiExceptionMapper> mapper = exceptionMapperRegistry.findMapperFor(e.getCause().getClass());
            if (!mapper.isPresent()) {
                throw e;
            }
            
            ErrorResponse errorResponse = mapper.get().toErrorResponse(e.getCause());
            response.setStatus(errorResponse.getHttpStatus());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            objectMapper.writeValue(baos, errorResponse.toResponse());

            try (OutputStream out = response.getOutputStream()) {
                out.write(baos.toByteArray());
                out.flush();
            }
            log.warn("Katharsis Invoker exception.", e);
        }
    }
}

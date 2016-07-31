package io.katharsis.rs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.JsonApiDispatcher;
import io.katharsis.dispatcher.ResponseContext;
import io.katharsis.errorhandling.exception.KatharsisInitializationException;
import io.katharsis.errorhandling.exception.KatharsisMatchingException;
import io.katharsis.repository.RepositoryParameterProvider;
import io.katharsis.request.Request;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.rs.parameterProvider.JaxRsParameterProvider;
import io.katharsis.rs.parameterProvider.RequestContextParameterProviderRegistry;
import io.katharsis.rs.type.JsonApiMediaType;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static io.katharsis.rs.type.JsonApiMediaType.APPLICATION_JSON_API_TYPE;

/**
 * Handles JSON API requests.
 * <p>
 * Consumes: <i>null</i> | {@link JsonApiMediaType}
 * Produces: {@link JsonApiMediaType}
 * </p>
 * <p>
 * Currently the response is sent using {@link ContainerRequestContext#abortWith(Response)} which might cause
 * problems with Jackson, co the serialization is happening in this filter.
 * </p>
 * <p>
 * To be able to send a request to Katharsis it is necessary to provide full media type alongside the request.
 * Wildcards are not accepted.
 * </p>
 */
@Slf4j
@PreMatching
public class KatharsisFilter implements ContainerRequestFilter {

    private ObjectMapper objectMapper;
    private JsonApiDispatcher requestDispatcher;
    private RequestContextParameterProviderRegistry parameterProviderRegistry;
    private String webPathPrefix;

    public KatharsisFilter(ObjectMapper objectMapper,
                           JsonApiDispatcher requestDispatcher,
                           RequestContextParameterProviderRegistry parameterProviderRegistry,
                           String webPathPrefix) {
        this.objectMapper = objectMapper;
        this.requestDispatcher = requestDispatcher;
        this.parameterProviderRegistry = parameterProviderRegistry;
        this.webPathPrefix = checkPath(webPathPrefix);
    }

    private static String checkPath(String webPathPrefix) {
        if (webPathPrefix != null && webPathPrefix.startsWith("/")) {
            return webPathPrefix;
        } else {
            throw new KatharsisInitializationException("API mount path must be absolute: " + webPathPrefix);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (requestContext.hasEntity() && !requestContext.getMediaType().isCompatible(JsonApiMediaType.APPLICATION_JSON_API_TYPE)) {
            return;
        }

        boolean acceptable = false;
        for (MediaType acceptableMediaType : requestContext.getAcceptableMediaTypes()) {
            if (acceptableMediaType.isCompatible(JsonApiMediaType.APPLICATION_JSON_API_TYPE)) {
                acceptable = true;
                break;
            }
        }

        if (!acceptable) {
            return;
        }

        try {
            dispatchRequest(requestContext);
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    private void dispatchRequest(ContainerRequestContext requestContext) throws Exception {
        UriInfo uriInfo = requestContext.getUriInfo();
        ResponseContext katharsisResponse = null;
        boolean passToMethodMatcher = false;
        //TODO: Refactor
        try {

            String httpMethod = requestContext.getMethod();

            RepositoryParameterProvider parameterProvider = new JaxRsParameterProvider(objectMapper, requestContext, parameterProviderRegistry);

            JsonApiPath path = JsonApiPath.parsePathFromStringUrl(uriInfo.getAbsolutePath().toURL(), webPathPrefix);

            Request request = new Request(path, httpMethod, requestContext.getEntityStream(), parameterProvider);

            katharsisResponse = requestDispatcher.handle(request);

        } catch (KatharsisMatchingException e) {
            passToMethodMatcher = true;
        } catch (Exception e) {
            log.error("Exception {}", e);
        } finally {
            if (!passToMethodMatcher) {
                abortWithResponse(requestContext, katharsisResponse);
            }
        }
    }

    private void abortWithResponse(ContainerRequestContext requestContext, ResponseContext katharsisResponse)
            throws IOException {
        Response response;
        if (katharsisResponse != null) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            objectMapper.writeValue(os, katharsisResponse);
            response = Response
                    .status(katharsisResponse.getHttpStatus())
                    .entity(new ByteArrayInputStream(os.toByteArray()))
                    .type(APPLICATION_JSON_API_TYPE)
                    .build();
        } else {
            response = Response.noContent().build();
        }
        requestContext.abortWith(response);
    }

}

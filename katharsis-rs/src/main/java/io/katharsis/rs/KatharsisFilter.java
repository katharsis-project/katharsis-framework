package io.katharsis.rs;

import static io.katharsis.rs.type.JsonApiMediaType.APPLICATION_JSON_API_TYPE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.annotation.Priority;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.dispatcher.RequestDispatcher;
import io.katharsis.errorhandling.exception.KatharsisMappableException;
import io.katharsis.errorhandling.exception.KatharsisMatchingException;
import io.katharsis.errorhandling.mapper.KatharsisExceptionMapper;
import io.katharsis.jackson.exception.JsonDeserializationException;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ServiceUrlProvider;
import io.katharsis.resource.registry.UriInfoServiceUrlProvider;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.rs.parameterProvider.JaxRsParameterProvider;
import io.katharsis.rs.parameterProvider.RequestContextParameterProviderRegistry;
import io.katharsis.rs.type.JsonApiMediaType;

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
@PreMatching
@Priority(Integer.MAX_VALUE) // Greatest value is applied last
public class KatharsisFilter implements ContainerRequestFilter {

	private Logger logger = LoggerFactory.getLogger(getClass());

    private ObjectMapper objectMapper;
    private ResourceRegistry resourceRegistry;
    private RequestDispatcher requestDispatcher;
    private RequestContextParameterProviderRegistry parameterProviderRegistry;
    private String webPathPrefix;

    public KatharsisFilter(ObjectMapper objectMapper,
                           QueryParamsBuilder queryParamsBuilder,
                           ResourceRegistry resourceRegistry, RequestDispatcher
            requestDispatcher, RequestContextParameterProviderRegistry parameterProviderRegistry, String webPathPrefix) {
        this.objectMapper = objectMapper;
        this.resourceRegistry = resourceRegistry;
        this.requestDispatcher = requestDispatcher;
        this.parameterProviderRegistry = parameterProviderRegistry;
        this.webPathPrefix = parsePrefix(webPathPrefix);
    }

    private static String parsePrefix(String webPathPrefix) {
        if (webPathPrefix != null && webPathPrefix.startsWith(PathBuilder.SEPARATOR)) {
            return webPathPrefix.substring(1);
        } else {
            return webPathPrefix;
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
        	logger.error("failed to dispatch request", e);
            throw e;
        } catch (Exception e) {
        	logger.error("failed to dispatch request", e);
            throw new WebApplicationException(e);
        }
    }

    private void dispatchRequest(ContainerRequestContext requestContext) throws Exception {
        UriInfo uriInfo = requestContext.getUriInfo();
        BaseResponseContext katharsisResponse = null;
        boolean passToMethodMatcher = false;
        ServiceUrlProvider serviceUrlProvider = resourceRegistry.getServiceUrlProvider();
        try {
            String path = buildPath(uriInfo);

            if(serviceUrlProvider instanceof UriInfoServiceUrlProvider){
            	// TODO not a particular nice way of doing this. With Katharsis 3.0 and the serialization
            	// refacotring there should be a better way achieving this. At that point
            	// serialization should be serialization only, no further logic like reading urls.
            	((UriInfoServiceUrlProvider)serviceUrlProvider).onRequestStarted(uriInfo);
            }

            JsonPath jsonPath = new PathBuilder(resourceRegistry).buildPath(path);

            Map<String, Set<String>> parameters = getParameters(uriInfo);

            String method = requestContext.getMethod();
            RequestBody requestBody = inputStreamToBody(requestContext.getEntityStream());

            JaxRsParameterProvider parameterProvider = new JaxRsParameterProvider(objectMapper, requestContext, parameterProviderRegistry);
            katharsisResponse = requestDispatcher
                .dispatchRequest(jsonPath, method, parameters, parameterProvider, requestBody);
        } catch (KatharsisMappableException e) {
        	logger.warn("failed to process request", e);
            katharsisResponse = new KatharsisExceptionMapper().toErrorResponse(e);
        } catch (KatharsisMatchingException e) {
        	logger.warn("failed to process request", e);
            passToMethodMatcher = true;
        } finally {
            if (!passToMethodMatcher) {
                abortWithResponse(requestContext, katharsisResponse);
            }

            if(serviceUrlProvider instanceof UriInfoServiceUrlProvider){
            	((UriInfoServiceUrlProvider)serviceUrlProvider).onRequestFinished();
            }
        }
    }

    private Map<String, Set<String>> getParameters(UriInfo uriInfo) {
    	 MultivaluedMap<String, String> queryParametersMultiMap = uriInfo.getQueryParameters();
         Map<String, Set<String>> queryParameters = new HashMap<>();

         for (Map.Entry<String, List<String>> queryEntry : queryParametersMultiMap.entrySet()) {
             queryParameters.put(queryEntry.getKey(), new LinkedHashSet<>(queryEntry.getValue()));
         }
         return queryParameters;
	}

	private String buildPath(UriInfo uriInfo) {
        String basePath = uriInfo.getPath();
        if (webPathPrefix != null && basePath.startsWith(webPathPrefix)) {
            return basePath.substring(webPathPrefix.length());
        } else {
            return basePath;
        }
    }

    private void abortWithResponse(ContainerRequestContext requestContext, BaseResponseContext katharsisResponse)
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



    public RequestBody inputStreamToBody(InputStream is) throws IOException {
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
}

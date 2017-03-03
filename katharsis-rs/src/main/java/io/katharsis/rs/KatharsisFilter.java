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

import io.katharsis.core.internal.dispatcher.RequestDispatcher;
import io.katharsis.core.internal.dispatcher.path.ActionPath;
import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.core.internal.dispatcher.path.PathBuilder;
import io.katharsis.core.internal.exception.KatharsisExceptionMapper;
import io.katharsis.errorhandling.exception.JsonDeserializationException;
import io.katharsis.errorhandling.exception.KatharsisMappableException;
import io.katharsis.errorhandling.exception.KatharsisMatchingException;
import io.katharsis.resource.Document;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ServiceUrlProvider;
import io.katharsis.rs.internal.parameterProvider.JaxRsParameterProvider;
import io.katharsis.rs.internal.parameterProvider.RequestContextParameterProviderRegistry;
import io.katharsis.rs.resource.registry.UriInfoServiceUrlProvider;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(KatharsisFilter.class);

    private ObjectMapper objectMapper;
    private ResourceRegistry resourceRegistry;
    private RequestDispatcher requestDispatcher;
    private RequestContextParameterProviderRegistry parameterProviderRegistry;
    private String webPathPrefix;

    public KatharsisFilter(ObjectMapper objectMapper,
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
        	LOGGER.error("failed to dispatch request", e);
            throw e;
        } catch (Exception e) {
        	LOGGER.error("failed to dispatch request", e);
            throw new WebApplicationException(e);
        }
    }

    private void dispatchRequest(ContainerRequestContext requestContext) throws Exception {
        UriInfo uriInfo = requestContext.getUriInfo();
        io.katharsis.repository.response.Response katharsisResponse = null;
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

            JsonPath jsonPath = new PathBuilder(resourceRegistry).build(path);
            Map<String, Set<String>> parameters = getParameters(uriInfo);
            String method = requestContext.getMethod();
            
            if(jsonPath instanceof ActionPath){
            	// inital implementation, has to improve
            	requestDispatcher.dispatchAction(jsonPath, method, parameters);
            	
            	// nothing further done, forward the call to JAX-RS
            	passToMethodMatcher = true;
            }else if(jsonPath != null){
	            Document requestBody = inputStreamToBody(requestContext.getEntityStream());
	
	            JaxRsParameterProvider parameterProvider = new JaxRsParameterProvider(objectMapper, requestContext, parameterProviderRegistry);
	            katharsisResponse = requestDispatcher
	                .dispatchRequest(jsonPath, method, parameters, parameterProvider, requestBody);
            }else{
            	// no repositories invoked, we do nothing and forward the call to JAX-RS
            	passToMethodMatcher = true;
            }
       
        } catch (KatharsisMappableException e) {
            // log error in KatharsisMappableException mapper.
            katharsisResponse = new KatharsisExceptionMapper().toErrorResponse(e).toResponse();
        } catch (KatharsisMatchingException e) {
        	LOGGER.warn("failed to process request", e);
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

    private void abortWithResponse(ContainerRequestContext requestContext, io.katharsis.repository.response.Response katharsisResponse)
        throws IOException {
        Response response;
        if (katharsisResponse != null) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            objectMapper.writeValue(os, katharsisResponse.getDocument());
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



    public Document inputStreamToBody(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String requestBody = s.hasNext() ? s.next() : "";
        if (requestBody == null || requestBody.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(requestBody, Document.class);
        } catch (IOException e) {
            throw new JsonDeserializationException(e.getMessage());
        }
    }
}

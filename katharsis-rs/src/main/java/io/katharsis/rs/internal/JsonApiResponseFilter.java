package io.katharsis.rs.internal;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.UriInfo;

import io.katharsis.core.internal.resource.DocumentMapper;
import io.katharsis.repository.response.JsonApiResponse;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.list.ResourceListBase;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ServiceUrlProvider;
import io.katharsis.rs.resource.registry.UriInfoServiceUrlProvider;

/**
 * Uses the Katharsis {@link DocumentMapper} to create a JSON API response for
 * custom JAX-RS actions returning Katharsis resources.
 */
public class JsonApiResponseFilter implements ContainerResponseFilter {

	private ResourceRegistry resourceRegistry;
	private DocumentMapper documentMapper;

	JsonApiResponseFilter(ResourceRegistry resourceRegistry, DocumentMapper documentMapper) {
		this.resourceRegistry = resourceRegistry;
		this.documentMapper = documentMapper;
	}

	/**
	 * Creates JSON API responses for custom JAX-RS actions returning Katharsis resources.
	 */
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		Object response = responseContext.getEntity();
		if (response == null) {
			return;
		}

		// only modify responses which contain a single or a list of Katharsis resources
		if (isResourceResponse(response)) {
			ServiceUrlProvider serviceUrlProvider = this.resourceRegistry.getServiceUrlProvider();
			try {
				UriInfo uriInfo = requestContext.getUriInfo();
				if (serviceUrlProvider instanceof UriInfoServiceUrlProvider) {
					((UriInfoServiceUrlProvider) serviceUrlProvider).onRequestStarted(uriInfo);
				}

				JsonApiResponse jsonApiResponse = new JsonApiResponse();
				jsonApiResponse.setEntity(response);
				// use the Katharsis document mapper to create a JSON API response
				responseContext.setEntity(documentMapper.toDocument(jsonApiResponse, null));
			}
			finally {
				if (serviceUrlProvider instanceof UriInfoServiceUrlProvider) {
					((UriInfoServiceUrlProvider) serviceUrlProvider).onRequestFinished();
				}
			}
		}
	}

	/**
	 * Determines whether the given response entity is either a Katharsis resource
	 * or a list of Katharsis resources.
	 * @param response the response entity
	 * @return <code>true</code>, if <code>response</code> is a (list of) Katharsis resource(s),<br />
	 * 			<code>false</code>, otherwise
	 */
	private boolean isResourceResponse(Object response) {
		boolean singleResource = response.getClass().getAnnotation(JsonApiResource.class) != null;
		boolean resourceList = ResourceListBase.class.isAssignableFrom(response.getClass());
		return singleResource || resourceList;
	}

}
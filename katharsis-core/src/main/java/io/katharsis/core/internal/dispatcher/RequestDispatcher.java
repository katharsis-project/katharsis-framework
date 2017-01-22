package io.katharsis.core.internal.dispatcher;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.katharsis.core.internal.dispatcher.controller.BaseController;
import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.core.internal.exception.ExceptionMapperRegistry;
import io.katharsis.core.internal.query.QueryAdapterBuilder;
import io.katharsis.errorhandling.exception.RepositoryNotFoundException;
import io.katharsis.errorhandling.exception.ResourceFieldNotFoundException;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;
import io.katharsis.legacy.internal.QueryParamsAdapter;
import io.katharsis.legacy.internal.RepositoryMethodParameterProvider;
import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.repository.filter.DocumentFilter;
import io.katharsis.repository.filter.DocumentFilterChain;
import io.katharsis.repository.filter.DocumentFilterContext;
import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.repository.response.Response;
import io.katharsis.resource.Document;
import io.katharsis.resource.information.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.utils.Optional;

/**
 * A class that can be used to integrate Katharsis with external frameworks like Jersey, Spring etc. See katharsis-rs
 * and katharsis-servlet for usage.
 */
public class RequestDispatcher {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private final ControllerRegistry controllerRegistry;

	private final ExceptionMapperRegistry exceptionMapperRegistry;

	private ModuleRegistry moduleRegistry;

	private QueryAdapterBuilder queryAdapterBuilder;

	public RequestDispatcher(ModuleRegistry moduleRegistry, ControllerRegistry controllerRegistry,
			ExceptionMapperRegistry exceptionMapperRegistry, QueryAdapterBuilder queryAdapterBuilder) {
		this.controllerRegistry = controllerRegistry;
		this.moduleRegistry = moduleRegistry;
		this.exceptionMapperRegistry = exceptionMapperRegistry;
		this.queryAdapterBuilder = queryAdapterBuilder;
	}

	/**
	 * Dispatch the request from a client
	 *
	 * @param jsonPath          built {@link JsonPath} instance which represents the URI sent in the request
	 * @param method       type of the request e.g. POST, GET, PATCH
	 * @param parameterProvider repository method parameter provider
	 * @param requestBody       deserialized body of the client request
	 * @return the response form the Katharsis
	 */
	public Response dispatchRequest(JsonPath jsonPath, String method, Map<String, Set<String>> parameters,
			RepositoryMethodParameterProvider parameterProvider,
			Document requestBody) {

		try {
			BaseController controller = controllerRegistry.getController(jsonPath, method);

			ResourceInformation resourceInformation = getRequestedResource(jsonPath);
			QueryAdapter queryAdapter = queryAdapterBuilder.build(resourceInformation, parameters);

			DefaultFilterRequestContext context = new DefaultFilterRequestContext(jsonPath, queryAdapter, parameterProvider,
					requestBody, method);
			DefaultFilterChain chain = new DefaultFilterChain(controller);
			return chain.doFilter(context);
		} catch (Exception e) {
			Optional<JsonApiExceptionMapper> exceptionMapper = exceptionMapperRegistry.findMapperFor(e.getClass());
			if (exceptionMapper.isPresent()) {
				//noinspection unchecked
				return exceptionMapper.get().toErrorResponse(e).toResponse();
			}else {
				logger.error("failed to process request", e);
				throw e;
			}
		}
	}

	private ResourceInformation getRequestedResource(JsonPath jsonPath) {
		ResourceRegistry resourceRegistry = moduleRegistry.getResourceRegistry();
		RegistryEntry registryEntry = resourceRegistry.getEntry(jsonPath.getResourceName());
		if (registryEntry == null) {
			throw new RepositoryNotFoundException(jsonPath.getResourceName());
		}
		String elementName = jsonPath.getElementName();
		if (elementName != null && !elementName.equals(jsonPath.getResourceName())) {
			ResourceField relationshipField = registryEntry.getResourceInformation().findRelationshipFieldByName(elementName);
			if (relationshipField == null) {
				throw new ResourceFieldNotFoundException(elementName);
			}
			String oppositeResourceType = relationshipField.getOppositeResourceType();
			return resourceRegistry.getEntry(oppositeResourceType).getResourceInformation();
		}else {
			return registryEntry.getResourceInformation();
		}
	}

	class DefaultFilterChain implements DocumentFilterChain {

		protected int filterIndex = 0;

		protected BaseController controller;

		public DefaultFilterChain(BaseController controller) {
			this.controller = controller;
		}

		@Override
		public Response doFilter(DocumentFilterContext context) {
			List<DocumentFilter> filters = moduleRegistry.getFilters();
			if (filterIndex == filters.size()) {
				return controller.handle(context.getJsonPath(), context.getQueryAdapter(), context.getParameterProvider(), context.getRequestBody());
			}
			else {
				DocumentFilter filter = filters.get(filterIndex);
				filterIndex++;
				return filter.filter(context, this);
			}
		}
	}
	
	class ActionFilterChain implements DocumentFilterChain {

		protected int filterIndex = 0;


		@Override
		public Response doFilter(DocumentFilterContext context) {
			List<DocumentFilter> filters = moduleRegistry.getFilters();
			if (filterIndex == filters.size()) {
				return null;
			}
			else {
				DocumentFilter filter = filters.get(filterIndex);
				filterIndex++;
				return filter.filter(context, this);
			}
		}
	}
	
	public void dispatchAction(JsonPath jsonPath, String method, Map<String, Set<String>> parameters) {
		// preliminary implementation, more to come in the future
		ActionFilterChain chain = new ActionFilterChain();
		
		DefaultFilterRequestContext context = new DefaultFilterRequestContext(jsonPath, null, null, null, method);
		chain.doFilter(context);		
	}

	class DefaultFilterRequestContext implements DocumentFilterContext {

		protected JsonPath jsonPath;

		protected QueryAdapter queryAdapter;

		protected RepositoryMethodParameterProvider parameterProvider;

		protected Document requestBody;

		private String method;

		public DefaultFilterRequestContext(JsonPath jsonPath, QueryAdapter queryAdapter,
				RepositoryMethodParameterProvider parameterProvider, Document requestBody, String method) {
			this.jsonPath = jsonPath;
			this.queryAdapter = queryAdapter;
			this.parameterProvider = parameterProvider;
			this.requestBody = requestBody;
			this.method = method;
		}

		@Override
		public Document getRequestBody() {
			return requestBody;
		}

		@Override
		public RepositoryMethodParameterProvider getParameterProvider() {
			return parameterProvider;
		}

		@Override
		public QueryParams getQueryParams() {
			return ((QueryParamsAdapter) queryAdapter).getQueryParams();
		}

		@Override
		public QueryAdapter getQueryAdapter() {
			return queryAdapter;
		}

		@Override
		public JsonPath getJsonPath() {
			return jsonPath;
		}

		@Override
		public String getMethod() {
			return method;
		}
	}

	public QueryAdapterBuilder getQueryAdapterBuilder() {
		return queryAdapterBuilder;
	}
}

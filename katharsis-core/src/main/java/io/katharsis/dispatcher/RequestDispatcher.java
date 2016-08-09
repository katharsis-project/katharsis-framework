package io.katharsis.dispatcher;

import java.util.List;

import io.katharsis.dispatcher.controller.BaseController;
import io.katharsis.dispatcher.filter.Filter;
import io.katharsis.dispatcher.filter.FilterChain;
import io.katharsis.dispatcher.filter.FilterRequestContext;
import io.katharsis.dispatcher.registry.ControllerRegistry;
import io.katharsis.errorhandling.mapper.ExceptionMapperRegistry;
import io.katharsis.errorhandling.mapper.JsonApiExceptionMapper;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.RepositoryMethodParameterProvider;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.utils.java.Optional;

/**
 * A class that can be used to integrate Katharsis with external frameworks like
 * Jersey, Spring etc. See katharsis-rs and katharsis-servlet for usage.
 */
public class RequestDispatcher {

	private final ModuleRegistry moduleRegistry;
	private final ControllerRegistry controllerRegistry;
	private final ExceptionMapperRegistry exceptionMapperRegistry;

	public RequestDispatcher(ModuleRegistry moduleRegistry, ControllerRegistry controllerRegistry,
			ExceptionMapperRegistry exceptionMapperRegistry) {
		this.moduleRegistry = moduleRegistry;
		this.controllerRegistry = controllerRegistry;
		this.exceptionMapperRegistry = exceptionMapperRegistry;
	}

	/**
	 * Dispatch the request from a client
	 *
	 * @param jsonPath
	 *            built {@link JsonPath} instance which represents the URI sent
	 *            in the request
	 * @param requestType
	 *            type of the request e.g. POST, GET, PATCH
	 * @param parameterProvider
	 *            repository method parameter provider
	 * @param queryParams
	 *            built object containing query parameters of the request
	 * @param requestBody
	 *            deserialized body of the client request
	 * @return the response form the Katharsis
	 */
	public BaseResponseContext dispatchRequest(JsonPath jsonPath, String requestType, QueryParams queryParams,
			RepositoryMethodParameterProvider parameterProvider,
			@SuppressWarnings("SameParameterValue") RequestBody requestBody) {

		try {

			BaseController controller = controllerRegistry.getController(jsonPath, requestType);

			DefaultFilterRequestContext context = new DefaultFilterRequestContext(jsonPath, queryParams,
					parameterProvider, requestBody);
			DefaultFilterChain chain = new DefaultFilterChain(controller);
			return chain.doFilter(context);
		} catch (Exception e) {
			Optional<JsonApiExceptionMapper> exceptionMapper = exceptionMapperRegistry.findMapperFor(e.getClass());
			if (exceptionMapper.isPresent()) {
				// noinspection unchecked
				return exceptionMapper.get().toErrorResponse(e);
			} else {
				throw e;
			}
		}
	}

	class DefaultFilterChain implements FilterChain{

		protected int filterIndex = 0;
		private List<Filter> filters;
		protected BaseController controller;
		
		public DefaultFilterChain(BaseController controller){
			this.controller = controller;
			this.filters = moduleRegistry.getFilters();
		}
		
		@Override
		public BaseResponseContext doFilter(FilterRequestContext context) {
			if (filterIndex == filters.size()) {
				return controller.handle(context.getJsonPath(), context.getQueryParams(), context.getParameterProvider(), context.getRequestBody());
			} else {
				Filter filter = filters.get(filterIndex);
				filterIndex++;
				return filter.filter(context, this);
			}
		}
	}
	
	class DefaultFilterRequestContext implements FilterRequestContext {
		
		protected JsonPath jsonPath;
		protected QueryParams queryParams;
		protected RepositoryMethodParameterProvider parameterProvider;
		protected RequestBody requestBody;

		public DefaultFilterRequestContext(JsonPath jsonPath, QueryParams queryParams,
				RepositoryMethodParameterProvider parameterProvider, RequestBody requestBody) {
			this.jsonPath = jsonPath;
			this.queryParams = queryParams;
			this.parameterProvider = parameterProvider;
			this.requestBody = requestBody;
		}

		@Override
		public RequestBody getRequestBody() {
			return requestBody;
		}

		@Override
		public RepositoryMethodParameterProvider getParameterProvider() {
			return parameterProvider;
		}

		@Override
		public QueryParams getQueryParams() {
			return queryParams;
		}

		@Override
		public JsonPath getJsonPath() {
			return jsonPath;
		}
	}
}

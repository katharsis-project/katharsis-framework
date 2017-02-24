package io.katharsis.security.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.katharsis.errorhandling.exception.ForbiddenException;
import io.katharsis.repository.filter.RepositoryFilterBase;
import io.katharsis.repository.filter.RepositoryFilterContext;
import io.katharsis.repository.filter.RepositoryMetaFilterChain;
import io.katharsis.repository.filter.RepositoryRequestFilterChain;
import io.katharsis.repository.request.HttpMethod;
import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.repository.request.RepositoryRequestSpec;
import io.katharsis.repository.response.JsonApiResponse;
import io.katharsis.resource.meta.MetaInformation;
import io.katharsis.security.ResourcePermission;
import io.katharsis.security.ResourcePermissionInformation;
import io.katharsis.security.SecurityModule;

public class SecurityFilter extends RepositoryFilterBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityFilter.class);

	private SecurityModule module;

	public SecurityFilter(SecurityModule module) {
		this.module = module;
	}

	@Override
	public JsonApiResponse filterRequest(RepositoryFilterContext context, RepositoryRequestFilterChain chain) {
		RepositoryRequestSpec request = context.getRequest();
		QueryAdapter queryAdapter = request.getQueryAdapter();
		Class<?> resourceClass = queryAdapter.getResourceInformation().getResourceClass();

		HttpMethod method = request.getMethod();
		ResourcePermission requiredPermission = ResourcePermission.fromMethod(method);

		boolean allowed = module.isAllowed(resourceClass, requiredPermission);
		if (!allowed) {
			String msg = "user not allowed to access " + resourceClass.getName();
			throw new ForbiddenException(msg);
		}
		else {
			LOGGER.debug("user allowed to access {}", resourceClass.getSimpleName());
			return chain.doFilter(context);
		}
	}

	@Override
	public <T> MetaInformation filterMeta(RepositoryFilterContext context, Iterable<T> resources,
			RepositoryMetaFilterChain chain) {
		MetaInformation metaInformation = chain.doFilter(context, resources);
		if (metaInformation instanceof ResourcePermissionInformation) {
			ResourcePermissionInformation permissionInformation = (ResourcePermissionInformation) metaInformation;

			QueryAdapter queryAdapter = context.getRequest().getQueryAdapter();
			Class<?> resourceClass = queryAdapter.getResourceInformation().getResourceClass();
			permissionInformation.setResourcePermission(module.getResourcePermission(resourceClass));
		}
		return metaInformation;
	}

}

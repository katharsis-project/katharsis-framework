package io.katharsis.rs.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.SecurityContext;

import io.katharsis.module.Module;
import io.katharsis.repository.information.RepositoryAction;
import io.katharsis.repository.information.internal.DefaultResourceRepositoryInformationBuilder;

public class JaxrsModule implements Module {

	private SecurityContext securityContext;

	private static final String ID_ACTION_PARAMETER = "{id}";

	public JaxrsModule(SecurityContext securityContext) {
		this.securityContext = securityContext;
	}

	@Override
	public void setupModule(ModuleContext context) {
		//		context.addExceptionMapper(new JaxrsNotAuthorizedExceptionMapper());
		//		context.addExceptionMapper(new JaxrsForbiddenExceptionMapper());

		context.addRepositoryInformationBuilder(new JaxrsResourceRepositoryInformationBuilder());

		if (securityContext != null) {
			context.addSecurityProvider(new JaxrsSecurityProvider(securityContext));
		}
	}

	@Override
	public String getModuleName() {
		return "jaxrs";
	}

	class JaxrsResourceRepositoryInformationBuilder extends DefaultResourceRepositoryInformationBuilder {

		@Override
		protected Map<String, RepositoryAction> buildActions(Class<? extends Object> repositoryClass) {
			HashMap<String, RepositoryAction> actions = new HashMap<>();
			for (Method method : repositoryClass.getMethods()) {

				Path pathAnnotation = method.getAnnotation(Path.class);
				boolean isGet = method.getAnnotation(GET.class) != null;
				boolean isPost = method.getAnnotation(POST.class) != null;
				boolean isPut = method.getAnnotation(PUT.class) != null;
				boolean isDelete = method.getAnnotation(DELETE.class) != null;

				boolean isJaxRs = isGet || isPost || isPut || isDelete;
				isJaxRs = isJaxRs || pathAnnotation != null;
				Annotation[][] parameterAnnotationsArray = method.getParameterAnnotations();

				for (int paramIndex = 0; paramIndex < parameterAnnotationsArray.length; paramIndex++) {
					Annotation[] parameterAnnotations = parameterAnnotationsArray[paramIndex];
					for (Annotation parameterAnnotation : parameterAnnotations) {
						isJaxRs = isJaxRs || parameterAnnotation instanceof PathParam
								|| parameterAnnotation instanceof QueryParam;
					}
				}

				if (pathAnnotation != null) {
					String path = normPath(pathAnnotation.value());
					String[] pathElements = path.split("\\/");
					if (pathElements.length == 0) {
						throw new IllegalStateException("@Path value must not be empty: " + method);
					}
					if (pathElements.length > 2) {
						throw new IllegalStateException("@Path value must not contain more than to elements: " + method);
					}

					if (pathElements.length == 1 && pathElements[0].equals(ID_ACTION_PARAMETER)) {
						throw new IllegalStateException("single element in @Path cannot be {id}, add action name: " + method);
					}
					if (pathElements.length == 2 && !pathElements[0].equals(ID_ACTION_PARAMETER)) {
						throw new IllegalStateException(
								"for two elements in @Path the first one must be {id}, the second the action name: " + method);
					}
					String name = pathElements[pathElements.length - 1];
					RepositoryAction action = new JaxrsRepositoryAction(name);
					actions.put(name, action);
				}
				else if (isJaxRs) {
					throw new IllegalStateException("JAXRS actions must be annotated with @Path: " + method);
				}
			}
			return actions;
		}

		private String normPath(String path) {
			String normPath = path;
			if (normPath.startsWith("/")) {
				normPath = normPath.substring(1);
			}
			if (normPath.endsWith("/")) {
				normPath = normPath.substring(0, normPath.length() - 1);
			}
			return normPath;
		}
	}

	class JaxrsRepositoryAction implements RepositoryAction {

		private String name;

		public JaxrsRepositoryAction(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}
	}
}

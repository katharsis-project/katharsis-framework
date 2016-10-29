package io.katharsis.rs.internal;

import javax.ws.rs.core.SecurityContext;

import io.katharsis.module.Module;

public class JaxrsModule implements Module {

	private SecurityContext securityContext;

	public JaxrsModule(SecurityContext securityContext) {
		this.securityContext = securityContext;
	}

	@Override
	public void setupModule(ModuleContext context) {
//		context.addExceptionMapper(new JaxrsNotAuthorizedExceptionMapper());
//		context.addExceptionMapper(new JaxrsForbiddenExceptionMapper());

		if (securityContext != null) {
			context.addSecurityProvider(new JaxrsSecurityProvider(securityContext));
		}
	}

	@Override
	public String getModuleName() {
		return "jaxrs";
	}

}

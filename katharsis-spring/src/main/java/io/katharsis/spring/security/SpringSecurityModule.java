package io.katharsis.spring.security;

import io.katharsis.module.Module;
import io.katharsis.spring.internal.AccessDeniedExceptionMapper;

/**
 * Module to register the Spring exception mappers with Katharsis.
 */
public class SpringSecurityModule implements Module {

	@Override
	public String getModuleName() {
		return "springSecurity";
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addExceptionMapper(new AccessDeniedExceptionMapper());
	}
}

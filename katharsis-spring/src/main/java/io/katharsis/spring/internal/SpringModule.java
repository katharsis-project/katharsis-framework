package io.katharsis.spring.internal;

import io.katharsis.module.Module;

/**
 * Module to register the Spring exception mappers with Katharsis.
 */
public class SpringModule implements Module {

	@Override
	public String getModuleName() {
		return "spring";
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addExceptionMapper(new AccessDeniedExceptionMapper());
	}
}

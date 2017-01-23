package io.katharsis.client.dynamic;

import io.katharsis.module.Module;

public class DynamicModule implements Module {

	@Override
	public String getModuleName() {
		return "dynamic";
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addRepositoryInformationBuilder(new DynamicRepositoryInformationBuilder());
		context.addRepository(new DynamicRepository());

	}

}

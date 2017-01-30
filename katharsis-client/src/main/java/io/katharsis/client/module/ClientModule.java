package io.katharsis.client.module;

import io.katharsis.core.internal.repository.information.DefaultRelationshipRepositoryInformationBuilder;
import io.katharsis.core.internal.repository.information.DefaultResourceRepositoryInformationBuilder;
import io.katharsis.core.internal.resource.AnnotationResourceInformationBuilder;
import io.katharsis.module.Module;
import io.katharsis.resource.information.ResourceFieldNameTransformer;

public class ClientModule implements Module {

	@Override
	public String getModuleName() {
		return "client";
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addResourceInformationBuilder(new AnnotationResourceInformationBuilder(new ResourceFieldNameTransformer()));
		context.addRepositoryInformationBuilder(new DefaultResourceRepositoryInformationBuilder());
		context.addRepositoryInformationBuilder(new DefaultRelationshipRepositoryInformationBuilder());
	}

}

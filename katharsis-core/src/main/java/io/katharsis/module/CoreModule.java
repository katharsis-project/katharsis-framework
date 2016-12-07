package io.katharsis.module;

import io.katharsis.errorhandling.mapper.DefaultExceptionMapperLookup;
import io.katharsis.repository.information.internal.DefaultRelationshipRepositoryInformationBuilder;
import io.katharsis.repository.information.internal.DefaultResourceRepositoryInformationBuilder;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
import io.katharsis.resource.registry.DefaultResourceLookup;

/**
 * Register the Katharsis core feature set as module.
 */
public class CoreModule extends SimpleModule {

	public static final String MODULE_NAME = "core";

	public CoreModule(String resourceSearchPackage, ResourceFieldNameTransformer resourceFieldNameTransformer) {
		this(resourceFieldNameTransformer);
		this.addResourceLookup(new DefaultResourceLookup(resourceSearchPackage));
		this.addExceptionMapperLookup(new DefaultExceptionMapperLookup(resourceSearchPackage));
	}
	
	public CoreModule(ResourceFieldNameTransformer resourceFieldNameTransformer) {
		super(MODULE_NAME);
		this.addResourceInformationBuilder(new AnnotationResourceInformationBuilder(resourceFieldNameTransformer));
		this.addRepositoryInformationBuilder(new DefaultResourceRepositoryInformationBuilder());
		this.addRepositoryInformationBuilder(new DefaultRelationshipRepositoryInformationBuilder());
	}
}

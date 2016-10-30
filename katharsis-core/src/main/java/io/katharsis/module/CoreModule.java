package io.katharsis.module;

import java.util.List;

import io.katharsis.errorhandling.mapper.DefaultExceptionMapperLookup;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
import io.katharsis.resource.registry.DefaultResourceLookup;

/**
 * Register the Katharsis core feature set as module.
 */
public class CoreModule extends SimpleModule {

	public static final String MODULE_NAME = "core";

	public CoreModule(List<String> resourceSearchPackages, ResourceFieldNameTransformer resourceFieldNameTransformer) {
		super(MODULE_NAME);
		this.addResourceLookup(new DefaultResourceLookup(resourceSearchPackages));
		this.addResourceInformationBuilder(new AnnotationResourceInformationBuilder(resourceFieldNameTransformer));
		this.addExceptionMapperLookup(new DefaultExceptionMapperLookup(resourceSearchPackages));
	}
	public CoreModule(String resourceSearchPackage, ResourceFieldNameTransformer resourceFieldNameTransformer) {
		super(MODULE_NAME);
		this.addResourceLookup(new DefaultResourceLookup(resourceSearchPackage));
		this.addResourceInformationBuilder(new AnnotationResourceInformationBuilder(resourceFieldNameTransformer));
		this.addExceptionMapperLookup(new DefaultExceptionMapperLookup(resourceSearchPackage));
	}
}

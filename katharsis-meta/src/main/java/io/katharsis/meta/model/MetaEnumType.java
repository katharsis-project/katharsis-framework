package io.katharsis.meta.model;

import io.katharsis.resource.annotations.JsonApiResource;

@JsonApiResource(type = "meta/enumType")
public class MetaEnumType extends MetaType {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object fromString(String value) { // NOSONAR
		Class<?> implClass = this.getImplementationClass();
		return Enum.valueOf((Class) implClass, value);
	}
}

package io.katharsis.meta.model;

import java.lang.reflect.Type;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.katharsis.core.internal.utils.ClassUtils;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToOne;

@JsonApiResource(type = "meta/type")
public class MetaType extends MetaElement {

	@JsonIgnore
	private Type implementationType;

	private MetaType elementType;

	@JsonIgnore
	public Class<?> getImplementationClass() {
		return ClassUtils.getRawType(implementationType);
	}

	public void setImplementationType(Type implementationType) {
		this.implementationType = implementationType;
	}

	public Type getImplementationType() {
		return implementationType;
	}

//	public Object fromString(String value) {
//		throw new UnsupportedOperationException();
//	}

	@JsonIgnore
	public boolean isCollection() {
		return this instanceof MetaCollectionType;
	}

	@JsonIgnore
	public MetaCollectionType asCollection() {
		return (MetaCollectionType) this;
	}

	@JsonIgnore
	public boolean isMap() {
		return this instanceof MetaMapType;
	}

	@JsonIgnore
	public MetaMapType asMap() {
		return (MetaMapType) this;
	}

	@JsonApiToOne
	public MetaType getElementType() {
		// FIXME move out
		if (elementType == null) {
			if (isCollection()) {
				return asCollection().getElementType();
			}
			else if (isMap()) {
				return asMap().getValueType();
			}
			else {
				return this;
			}
		}
		return elementType;
	}

	public void setElementType(MetaType elementType) {
		this.elementType = elementType;
	}
}

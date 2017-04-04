package io.katharsis.meta.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.katharsis.resource.annotations.JsonApiResource;

@JsonApiResource(type = "meta/collectionType")
public abstract class MetaCollectionType extends MetaType {

	@JsonIgnore
	public <T> Collection<T> newInstance() {
		if (getImplementationClass() == Set.class)
			return new HashSet<>();
		if (getImplementationClass() == List.class)
			return new ArrayList<>();
		throw new UnsupportedOperationException(getImplementationClass().getName());
	}

}

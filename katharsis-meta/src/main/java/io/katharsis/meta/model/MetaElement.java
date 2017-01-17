package io.katharsis.meta.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToMany;
import io.katharsis.resource.annotations.JsonApiToOne;

@JsonApiResource(type = "meta/element")
public class MetaElement {

	@JsonApiId
	private String id;

	private String name;

	@JsonApiToOne(opposite = "children")
	private MetaElement parent;

	@JsonApiToMany(opposite = "parent")
	private List<MetaElement> children = new ArrayList<>();

	public MetaElement getParent() {
		return parent;
	}

	public List<MetaElement> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public void setChildren(List<MetaElement> children) {
		this.children = children;
	}

	public void addChild(MetaElement child) {
		children.add(child);
	}

	public MetaType asType() {
		if (!(this instanceof MetaType))
			throw new IllegalStateException(getName() + " not a MetaEntity");
		return (MetaType) this;
	}

	public MetaDataObject asDataObject() {
		if (!(this instanceof MetaDataObject))
			throw new IllegalStateException(getName() + " not a MetaDataObject");
		return (MetaDataObject) this;
	}

	public void setParent(MetaElement parent) {
		setParent(parent, true);
	}

	public void setParent(MetaElement parent, boolean attach) {
		this.parent = parent;

		if (parent != null && attach) {
			parent.addChild(this);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name=" + getName() + "]";
	}

	public final String getId() {
		if (id == null) {
			throw new UnsupportedOperationException("id not available for " + toString());
		}
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonIgnore
	public boolean hasId() {
		return id != null;
	}
}

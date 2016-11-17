package io.katharsis.jpa.internal.meta.impl;

import java.util.ArrayList;
import java.util.List;

import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaElement;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.meta.MetaType;

public abstract class MetaElementImpl implements MetaElement {

	private MetaElement parent;

	private List<MetaElement> children = new ArrayList<>();

	protected MetaLookup lookup;

	public MetaElementImpl(MetaElement parent) {
		this.parent = parent;
		if (parent != null) {
			List<MetaElement> parentsChildren = parent.getChildren();
			parentsChildren.add(this);
		}
	}
	
	@Override
	public MetaLookup getLookup(){
		return lookup;
	}

	@Override
	public void init(MetaLookup lookup) {
		this.lookup = lookup;
		for (MetaElement child : children) {
			((MetaElementImpl) child).init(lookup);
		}
	}

	@Override
	public MetaElement getParent() {
		return parent;
	}

	@Override
	public List<MetaElement> getChildren() {
		return children;
	}

	@Override
	public MetaEntity asEntity() {
		if (!(this instanceof MetaEntity))
			throw new IllegalStateException(getName() + " not a MetaEntity");
		return (MetaEntity) this;
	}

	@Override
	public MetaType asType() {
		if (!(this instanceof MetaType))
			throw new IllegalStateException(getName() + " not a MetaEntity");
		return (MetaType) this;
	}

	@Override
	public MetaDataObject asDataObject() {
		if (!(this instanceof MetaDataObject))
			throw new IllegalStateException(getName() + " not a MetaDataObject");
		return (MetaDataObject) this;
	}

	public void setParent(MetaElementImpl parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name=" + getName() + "]";
	}
}

package io.katharsis.jpa.internal.meta.impl;

import java.util.ArrayList;
import java.util.List;

import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaElement;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaType;

public abstract class MetaElementImpl implements MetaElement {

	private MetaElementImpl parent;
	private List<MetaElementImpl> children = new ArrayList<MetaElementImpl>();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MetaElementImpl(MetaElementImpl parent) {
		this.parent = parent;
		if (parent != null) {
			List children = parent.getChildren();
			children.add(this);
		}
	}

	/**
	 * Initializes the given element. Usually relations to other meta elements
	 * are resolved here to allow cyclic dependencies between elements not
	 * possible within constructors.
	 */
	public void init() {
		for (MetaElement child : children) {
			((MetaElementImpl) child).init();
		}
	}

	@Override
	public MetaElement getParent() {
		return parent;
	}

	@Override
	public List<? extends MetaElement> getChildren() {
		return children;
	}

	@Override
	public abstract String getName();

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

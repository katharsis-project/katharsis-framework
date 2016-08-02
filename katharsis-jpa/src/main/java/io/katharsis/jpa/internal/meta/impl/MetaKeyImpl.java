package io.katharsis.jpa.internal.meta.impl;

import java.util.List;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaKey;
import io.katharsis.jpa.internal.meta.MetaType;

public class MetaKeyImpl extends MetaElementImpl implements MetaKey {

	private String name;
	private List<MetaAttribute> elements;
	private boolean primaryKey;
	private boolean unique;
	private MetaType type;

	public MetaKeyImpl(MetaDataObjectImpl parent, String name, List<MetaAttribute> elements, boolean primaryKey,
			boolean unique, MetaType type) {
		super(parent);
		this.name = name;
		this.elements = elements;
		this.primaryKey = primaryKey;
		this.unique = unique;
		this.type = type;
	}

	@Override
	public String getId() {
		return getParent().getId() + "." + getName();
	}

	@Override
	public List<MetaAttribute> getElements() {
		return elements;
	}

	@Override
	public boolean isUnique() {
		return unique;
	}

	@Override
	public boolean isPrimaryKey() {
		return primaryKey;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public MetaType getType() {
		return type;
	}

	@Override
	public MetaAttribute getUniqueElement() {
		if (elements.size() != 1)
			throw new IllegalStateException(getName() + " must contain a single primary key attribute");
		return elements.get(0);
	}

}

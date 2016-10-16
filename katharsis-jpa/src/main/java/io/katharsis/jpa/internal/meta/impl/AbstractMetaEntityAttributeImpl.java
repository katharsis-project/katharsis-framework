package io.katharsis.jpa.internal.meta.impl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

import javax.persistence.ElementCollection;
import javax.persistence.EmbeddedId;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.meta.MetaType;

public class AbstractMetaEntityAttributeImpl extends MetaAttributeImpl {

	protected Field field;

	private boolean derived;

	private boolean lazy = false;

	private String mappedBy = null;

	private MetaAttribute oppositeAttr;

	private boolean version = false;

	private boolean idField;

	public AbstractMetaEntityAttributeImpl(MetaDataObjectImpl parent, PropertyDescriptor desc) {
		super(parent, desc);

		field = getField(parent, desc);
		if (field != null) {
			readAnnotations(field);
		}
		else {
			derived = true;
		}
	}

	private static Field getField(MetaDataObjectImpl parent, PropertyDescriptor desc) {
		try {
			Field field = parent.getImplementationClass().getDeclaredField(desc.getName());
			field.setAccessible(true);
			return field;
		}
		catch (NoSuchFieldException e) { // NOSONAR
			return null;
		}
	}

	private void readAnnotations(Field field) { // NOSONAR
		ManyToMany manyManyAnnotation = field.getAnnotation(ManyToMany.class);
		ManyToOne manyOneAnnotation = field.getAnnotation(ManyToOne.class);
		OneToMany oneManyAnnotation = field.getAnnotation(OneToMany.class);
		OneToOne oneOneAnnotation = field.getAnnotation(OneToOne.class);
		Version versionAnnotation = field.getAnnotation(Version.class);
		ElementCollection elemCollectionAnnotation = field.getAnnotation(ElementCollection.class);

		version = versionAnnotation != null;

		FetchType fetchType = null;
		if (manyManyAnnotation != null) {
			mappedBy = manyManyAnnotation.mappedBy();
			fetchType = manyManyAnnotation.fetch();
		}
		if (oneManyAnnotation != null) {
			mappedBy = oneManyAnnotation.mappedBy();
			fetchType = oneManyAnnotation.fetch();
		}
		if (oneOneAnnotation != null) {
			mappedBy = oneOneAnnotation.mappedBy();
			fetchType = oneOneAnnotation.fetch();
		}

		if (mappedBy != null && mappedBy.length() == 0) {
			mappedBy = null;
		}

		setAssociation(
				manyManyAnnotation != null || manyOneAnnotation != null || oneManyAnnotation != null || oneOneAnnotation != null);

		boolean lazyCollection = elemCollectionAnnotation != null && elemCollectionAnnotation.fetch() != FetchType.EAGER;
		boolean lazyAssociation = isAssociation() && (fetchType == null || fetchType == FetchType.LAZY);

		lazy = lazyCollection || lazyAssociation;

		idField = field.getAnnotation(EmbeddedId.class) != null || field.getAnnotation(Id.class) != null;
	}

	@Override
	public boolean isId() {
		return idField;
	}

	@Override
	public MetaAttribute getOppositeAttribute() {
		return oppositeAttr;
	}

	@Override
	public void init(MetaLookup lookup) {
		super.init(lookup);
		if (mappedBy != null) {
			MetaType mappedType = getType();
			if (mappedType.isCollection()) {
				mappedType = mappedType.asCollection().getElementType();
			}
			MetaEntity type = mappedType.asEntity();
			oppositeAttr = type.getAttribute(mappedBy);
			((AbstractMetaEntityAttributeImpl) oppositeAttr).oppositeAttr = this;
		}
	}

	@Override
	public boolean isLazy() {
		return lazy;
	}

	@Override
	public String getId() {
		return getParent() + "." + getName();
	}

	@Override
	public boolean isDerived() {
		return derived;
	}

	@Override
	public boolean isVersion() {
		return version;
	}
}
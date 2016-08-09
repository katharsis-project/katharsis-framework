package io.katharsis.jpa.internal.meta.impl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Collection;

import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaType;

public class AbstractMetaEntityAttributeImpl extends MetaAttributeImpl {

	protected Field field;
	protected boolean association;
	private boolean derived;

	private boolean lazy = false;
	private String mappedBy = null;
	private MetaAttribute oppositeAttr;
	private boolean version = false;

	public AbstractMetaEntityAttributeImpl(MetaDataObjectImpl parent, PropertyDescriptor desc) {
		super(parent, desc);

		try {
			field = parent.getImplementationClass().getDeclaredField(desc.getName());
			field.setAccessible(true);

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
			}
			if (oneManyAnnotation != null) {
				mappedBy = oneManyAnnotation.mappedBy();
			}
			if (oneOneAnnotation != null) {
				mappedBy = oneOneAnnotation.mappedBy();
				oneOneAnnotation.fetch();
			}

			if (mappedBy != null && mappedBy.length() == 0) {
				mappedBy = null;
			}

			association = manyManyAnnotation != null || manyOneAnnotation != null || oneManyAnnotation != null
					|| oneOneAnnotation != null;

			lazy = (elemCollectionAnnotation != null && elemCollectionAnnotation.fetch() != FetchType.EAGER)
					|| (association && (fetchType == null || fetchType == FetchType.LAZY));

		} catch (NoSuchFieldException e) {
			derived = true;
		}
	}

	@Override
	public MetaAttribute getOppositeAttribute() {
		return oppositeAttr;
	}

	@Override
	public void init() {
		super.init();
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

	public boolean isLazy() {
		return lazy;
	}

	public String getName() {
		return desc.getName();
	}

	public boolean isAssociation() {
		return association;
	}

	@Override
	public String getId() {
		return getParent() + "." + field.getName();
	}

	@Override
	public boolean isDerived() {
		return derived;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addValue(Object dataObject, Object value) {
		Collection col = (Collection) getValue(dataObject);
		col.add(value);
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void removeValue(Object dataObject, Object value) {
		Collection col = (Collection) getValue(dataObject);
		col.remove(value);
	}

	@Override
	public boolean isVersion() {
		return version;
	}
}
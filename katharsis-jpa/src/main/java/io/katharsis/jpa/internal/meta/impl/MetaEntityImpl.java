package io.katharsis.jpa.internal.meta.impl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Id;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaCollectionType;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaKey;
import io.katharsis.jpa.internal.meta.MetaType;

public class MetaEntityImpl extends MetaDataObjectImpl implements MetaEntity {

	private static final String PK_NAME = "_primaryKey";

	private MetaKey primaryKey;
	private List<MetaKey> declaredKeys;

	public MetaEntityImpl(Class<?> implClass, Type implType, MetaDataObjectImpl superType) {
		super(implClass, implType, superType);
	}

	@Override
	protected MetaEntityAttributeImpl newAttributeAttribute(MetaDataObjectImpl metaDataObject,
			PropertyDescriptor desc) {
		return new MetaEntityAttributeImpl(this, desc);
	}

	@Override
	public String getName() {
		return getImplementationClass().getSimpleName();
	}

	@Override
	public MetaKey getPrimaryKey() {
		if (getSuperType() != null)
			return getSuperType().getPrimaryKey();
		checkInitialized();
		return primaryKey;
	}

	private void checkInitialized() {
		if (declaredKeys == null) {
			Field[] fields = getImplementationClass().getDeclaredFields();
			ArrayList<MetaAttribute> pkElements = new ArrayList<MetaAttribute>();
			for (Field field : fields) {
				if (field.getAnnotation(Id.class) != null) {
					pkElements.add(getAttribute(field.getName()));
				}
			}
			if (pkElements.size() > 0) {
				MetaType type;
				if (pkElements.size() == 1) {
					type = pkElements.get(0).getType();
				} else {
					throw new IllegalStateException("not supported");
				}

				primaryKey = new MetaKeyImpl(this, PK_NAME, pkElements, true, true, type);
			}

			// TODO
			declaredKeys = new ArrayList<MetaKey>();
		}

	}

	@Override
	public Set<MetaKey> getKeys() {
		throw new IllegalStateException("not implemented");
	}

	@Override
	public Object fromString(String values) {
		throw new UnsupportedOperationException("no yet implemented");
	}

}
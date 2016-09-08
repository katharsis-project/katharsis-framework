package io.katharsis.jpa.internal.meta.impl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EmbeddedId;
import javax.persistence.Id;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaEntity;
import io.katharsis.jpa.internal.meta.MetaKey;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.meta.MetaType;

public class AbstractMetaEntityImpl extends MetaDataObjectImpl {

	private static final String PK_NAME = "_primaryKey";

	private MetaKey primaryKey;
	private Set<MetaKey> declaredKeys;

	public AbstractMetaEntityImpl(Class<?> implClass, Type implType, MetaDataObjectImpl superType) {
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
		if (getSuperType() instanceof MetaEntity)
			return getSuperType().getPrimaryKey();
		return primaryKey;
	}

	@Override
	public void init(MetaLookup lookup) {
		super.init(lookup);
		if (declaredKeys == null) {
			Field[] fields = getImplementationClass().getDeclaredFields();
			ArrayList<MetaAttribute> pkElements = new ArrayList<>();
			for (Field field : fields) {
				if (field.getAnnotation(EmbeddedId.class) != null || field.getAnnotation(Id.class) != null) {
					pkElements.add(getAttribute(field.getName()));
				}
			}
			if (!pkElements.isEmpty()) {
				MetaType type;
				if (pkElements.size() == 1) {
					type = pkElements.get(0).getType();
				} else {
					throw new IllegalStateException("not supported");
				}

				primaryKey = new MetaKeyImpl(this, PK_NAME, pkElements, true, true, type);
			}

			// TODO parse key annotations
			declaredKeys = new HashSet<>();
		}

	}

	@Override
	public Set<MetaKey> getKeys() {
		return declaredKeys;
	}

	@Override
	public Object fromString(String values) {
		throw new UnsupportedOperationException("no yet implemented");
	}

}

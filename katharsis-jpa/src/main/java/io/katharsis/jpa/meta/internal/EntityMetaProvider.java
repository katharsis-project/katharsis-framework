package io.katharsis.jpa.meta.internal;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;

import io.katharsis.core.internal.utils.ClassUtils;
import io.katharsis.jpa.meta.MetaEntity;
import io.katharsis.jpa.meta.MetaJpaDataObject;
import io.katharsis.meta.model.MetaElement;

public class EntityMetaProvider extends AbstractEntityMetaProvider<MetaEntity> {

	@Override
	public Set<Class<? extends MetaElement>> getMetaTypes() {
		Set<Class<? extends MetaElement>> set = new HashSet<>();
		set.add(MetaEntity.class);
		return set;
	}

	@Override
	public boolean accept(Type type, Class<? extends MetaElement> metaClass) {
		boolean hasAnnotation = ClassUtils.getRawType(type).getAnnotation(Entity.class) != null;
		boolean hasMetaType = metaClass == MetaElement.class || metaClass == MetaEntity.class || metaClass == MetaJpaDataObject.class;
		return hasAnnotation && hasMetaType;
	}

	@Override
	protected MetaEntity newDataObject() {
		return new MetaEntity();
	}

}
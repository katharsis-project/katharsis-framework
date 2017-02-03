package io.katharsis.jpa.meta.internal;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.MappedSuperclass;

import io.katharsis.core.internal.utils.ClassUtils;
import io.katharsis.jpa.meta.MetaJpaDataObject;
import io.katharsis.jpa.meta.MetaMappedSuperclass;
import io.katharsis.meta.model.MetaElement;

public class MappedSuperclassMetaProvider extends AbstractEntityMetaProvider<MetaMappedSuperclass> {

	@Override
	public Set<Class<? extends MetaElement>> getMetaTypes() {
		Set<Class<? extends MetaElement>> set = new HashSet<>();
		set.add(MetaMappedSuperclass.class);
		return set;
	}

	@Override
	public boolean accept(Type type, Class<? extends MetaElement> metaClass) {
		boolean hasAnnotation = ClassUtils.getRawType(type).getAnnotation(MappedSuperclass.class) != null;
		boolean hasType = metaClass == MetaElement.class || metaClass == MetaMappedSuperclass.class || metaClass == MetaJpaDataObject.class;
		return hasAnnotation && hasType;
	}

	@Override
	protected MetaMappedSuperclass newDataObject() {
		return new MetaMappedSuperclass();
	}

}
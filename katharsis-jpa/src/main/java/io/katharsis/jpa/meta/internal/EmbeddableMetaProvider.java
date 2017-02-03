package io.katharsis.jpa.meta.internal;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Embeddable;

import io.katharsis.core.internal.utils.ClassUtils;
import io.katharsis.jpa.meta.MetaEmbeddable;
import io.katharsis.jpa.meta.MetaEmbeddableAttribute;
import io.katharsis.jpa.meta.MetaJpaDataObject;
import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.meta.model.MetaDataObject;
import io.katharsis.meta.model.MetaElement;
import io.katharsis.meta.provider.MetaProviderContext;

public class EmbeddableMetaProvider extends AbstractJpaDataObjectProvider<MetaEmbeddable> {

	@Override
	public Set<Class<? extends MetaElement>> getMetaTypes() {
		Set<Class<? extends MetaElement>> set = new HashSet<>();
		set.add(MetaEmbeddable.class);
		return set;
	}

	@Override
	public boolean accept(Type type, Class<? extends MetaElement> metaClass) {
		boolean hasAnnotation = ClassUtils.getRawType(type).getAnnotation(Embeddable.class) != null;
		boolean hasType = metaClass == MetaElement.class || metaClass == MetaEmbeddable.class || metaClass == MetaJpaDataObject.class;
		return hasAnnotation && hasType;
	}

	@Override
	public MetaEmbeddable createElement(Type type, MetaProviderContext context) {
		Class<?> rawClazz = ClassUtils.getRawType(type);
		Class<?> superClazz = rawClazz.getSuperclass();
		MetaElement superMeta = null;
		if (superClazz != Object.class) {
			superMeta = context.getLookup().getMeta(superClazz, MetaJpaDataObject.class);
		}
		MetaEmbeddable meta = new MetaEmbeddable();
		meta.setName(rawClazz.getSimpleName());
		meta.setImplementationType(type);
		meta.setSuperType((MetaDataObject) superMeta);
		createAttributes(meta);
		return meta;
	}

	@Override
	protected MetaAttribute createAttribute(MetaEmbeddable metaDataObject, PropertyDescriptor desc) {
		MetaEmbeddableAttribute attr = new MetaEmbeddableAttribute();
		attr.setParent(metaDataObject);
		attr.setName(desc.getName());
		return attr;
	}
}
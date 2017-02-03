package io.katharsis.meta.internal;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import io.katharsis.core.internal.utils.ClassUtils;
import io.katharsis.core.internal.utils.PropertyUtils;
import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.meta.model.MetaDataObject;
import io.katharsis.meta.model.MetaElement;
import io.katharsis.meta.provider.MetaProviderContext;

public abstract class MetaDataObjectProvider extends MetaDataObjectProviderBase<MetaDataObject> {

	@Override
	public MetaElement createElement(Type type, MetaProviderContext context) {
		Class<?> rawClazz = ClassUtils.getRawType(type);
		Class<?> superClazz = rawClazz.getSuperclass();
		MetaElement superMeta = null;
		if (superClazz != Object.class && superClazz != null) {
			superMeta = context.getLookup().getMeta(superClazz, getMetaClass());
		}
		MetaDataObject meta = newDataObject();
		meta.setName(rawClazz.getSimpleName());
		meta.setImplementationType(type);
		meta.setSuperType((MetaDataObject) superMeta);
		createAttributes(meta);
		return meta;
	}

	protected abstract MetaDataObject newDataObject();

	@Override
	public void onInitialized(MetaProviderContext context, MetaElement element) {
		if (element instanceof MetaAttribute && element.getParent().getClass() == getMetaClass()) {
			MetaAttribute attr = (MetaAttribute) element;
			MetaDataObject parent = attr.getParent();
			Type implementationType = PropertyUtils.getPropertyType(parent.getImplementationClass(), attr.getName());
			MetaElement metaType = context.getLookup().getMeta(implementationType, getMetaClass(), true);
			if(metaType == null){
				 metaType = context.getLookup().getMeta(implementationType);
			}
			attr.setType(metaType.asType());
		}
	}

	protected abstract Class<? extends MetaElement> getMetaClass();

	@Override
	public Set<Class<? extends MetaElement>> getMetaTypes() {
		Set<Class<? extends MetaElement>> set = new HashSet<>();
		set.add(getMetaClass());
		return set;
	}

}

package io.katharsis.jpa.meta.internal;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import javax.persistence.Embeddable;

import io.katharsis.core.internal.utils.PropertyUtils;
import io.katharsis.jpa.meta.MetaJpaDataObject;
import io.katharsis.meta.internal.MetaDataObjectProviderBase;
import io.katharsis.meta.model.MetaAttribute;
import io.katharsis.meta.model.MetaDataObject;
import io.katharsis.meta.model.MetaElement;
import io.katharsis.meta.model.MetaType;
import io.katharsis.meta.provider.MetaProviderContext;

public abstract class AbstractJpaDataObjectProvider<T extends MetaJpaDataObject> extends MetaDataObjectProviderBase<T> {

	@Override
	public void onInitialized(MetaProviderContext context, MetaElement element) {
		super.onInitialized(context, element);
		if (element.getParent() instanceof MetaJpaDataObject && element instanceof MetaAttribute) {
			MetaAttribute attr = (MetaAttribute) element;
			MetaDataObject parent = attr.getParent();
			Type implementationType = PropertyUtils.getPropertyType(parent.getImplementationClass(), attr.getName());

			Class<?> elementType = getElementType(implementationType);

			boolean jpaObject = attr.isAssociation() || elementType.getAnnotation(Embeddable.class) != null;

			Class<? extends MetaType> metaClass = jpaObject ? MetaJpaDataObject.class : MetaType.class;
			MetaType metaType = context.getLookup().getMeta(implementationType, metaClass);
			attr.setType(metaType);
		}
	}

	private Class<?> getElementType(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType) type;
			if (paramType.getRawType() instanceof Class && Map.class.isAssignableFrom((Class<?>) paramType.getRawType())) {
				return getElementType(paramType.getActualTypeArguments()[1]);
			}
			if (paramType.getRawType() instanceof Class && Collection.class.isAssignableFrom((Class<?>) paramType.getRawType())) {
				return getElementType(paramType.getActualTypeArguments()[0]);
			}
		}
		throw new IllegalArgumentException(type.toString());
	}
}

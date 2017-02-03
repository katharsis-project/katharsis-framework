package io.katharsis.meta.internal;

import java.lang.reflect.Type;

import io.katharsis.core.internal.utils.ClassUtils;
import io.katharsis.meta.model.MetaDataObject;
import io.katharsis.meta.model.MetaElement;
import io.katharsis.meta.model.resource.MetaJsonObject;
import io.katharsis.resource.annotations.JsonApiResource;

public class JsonObjectMetaProvider extends MetaDataObjectProvider {

	@Override
	public boolean accept(Type type, Class<? extends MetaElement> metaClass) {
		boolean hasResourceAnnotation = ClassUtils.getRawType(type).getAnnotation(JsonApiResource.class) != null;
		return type instanceof Class && metaClass == MetaJsonObject.class && !hasResourceAnnotation;
	}

	@Override
	protected MetaDataObject newDataObject() {
		return new MetaJsonObject();
	}

	@Override
	protected Class<? extends MetaElement> getMetaClass() {
		return MetaJsonObject.class;
	}
}

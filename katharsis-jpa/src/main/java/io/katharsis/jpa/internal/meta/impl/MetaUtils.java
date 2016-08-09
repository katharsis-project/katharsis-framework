package io.katharsis.jpa.internal.meta.impl;

import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaElement;
import io.katharsis.jpa.internal.meta.MetaLookup;

public class MetaUtils {

	// FIXME remo DECIDE: we should have proper meta data classes

	public static String toString(Object entity) {
		if (entity == null) {
			return null;
		}

		Class<? extends Object> clazz = entity.getClass();

		MetaElement meta = MetaLookup.INSTANCE.getMeta(clazz);
		MetaDataObject metaDataObject = meta.asDataObject();
		return metaDataObject.toString(entity);
	}

	@Deprecated
	public static boolean isMapKeyAttribute(String attributeName) {
		return attributeName.startsWith("key:");
	}
}

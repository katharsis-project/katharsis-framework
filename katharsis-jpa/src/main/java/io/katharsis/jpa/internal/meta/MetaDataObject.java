package io.katharsis.jpa.internal.meta;

import java.util.List;
import java.util.Set;

public interface MetaDataObject extends MetaType {

	MetaAttribute getAttribute(String name);

	String toString(Object dataObject);

	List<? extends MetaAttribute> getAttributes();

	List<? extends MetaAttribute> getDeclaredAttributes();

	MetaAttributePath resolvePath(String attrPath, boolean searchSubTypes);

	MetaDataObject getSuperType();

	MetaDataObject getRootType();

	List<? extends MetaDataObject> getSubTypes(boolean transitive, boolean self);

	List<? extends MetaDataObject> getSubTypes();

	MetaDataObject findSubTypeOrSelf(Class<?> implClass);

	MetaAttributePath resolvePath(String attrPath);

	MetaKey getPrimaryKey();

	Set<MetaKey> getKeys();

	MetaProjection asProjection();

	boolean hasAttribute(String name);

}

package io.katharsis.jpa.internal.meta;

import java.util.List;
import java.util.Set;

public interface MetaDataObject extends MetaType {

	MetaAttribute getAttribute(String name);

	MetaAttribute findAttribute(String name, boolean includeSubTypes);

	String toString(Object dataObject);

	<T extends MetaAttribute> List<T> getAttributes();

	<T extends MetaAttribute> List<T> getDeclaredAttributes();

	MetaAttributePath resolvePath(List<String> attrPath, boolean searchSubTypes);

	MetaDataObject getSuperType();

	MetaDataObject getRootType();

	<T extends MetaDataObject> List<T> getSubTypes(boolean transitive, boolean self);

	<T extends MetaDataObject> List<T> getSubTypes();

	MetaDataObject findSubTypeOrSelf(Class<?> implClass);

	MetaAttributePath resolvePath(List<String> attrPath);

	MetaKey getPrimaryKey();

	Set<MetaKey> getKeys();

	MetaProjection asProjection();

	boolean hasAttribute(String name);

	MetaAttribute getVersionAttribute();

	MetaAttributePath resolvePath(List<String> attrPath, MetaAttributeFinder finder);

	boolean isAbstract();

}

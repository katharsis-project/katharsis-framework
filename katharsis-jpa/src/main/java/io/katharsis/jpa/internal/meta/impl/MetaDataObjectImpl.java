package io.katharsis.jpa.internal.meta.impl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaAttributeFinder;
import io.katharsis.jpa.internal.meta.MetaAttributePath;
import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaKey;
import io.katharsis.jpa.internal.meta.MetaMapAttribute;
import io.katharsis.jpa.internal.meta.MetaMapType;
import io.katharsis.jpa.internal.meta.MetaType;
import io.katharsis.utils.PreconditionUtil;

public class MetaDataObjectImpl extends MetaTypeImpl implements MetaDataObject {

	private static final MetaAttributeFinder DEFAULT_ATTRIBUTE_FINDER = new MetaAttributeFinder() {

		@Override
		public MetaAttribute getAttribute(MetaDataObject meta, String name) {
			return meta.getAttribute(name);
		}
	};

	private static final MetaAttributeFinder SUBTYPE_ATTRIBUTE_FINDER = new MetaAttributeFinder() {

		@Override
		public MetaAttribute getAttribute(MetaDataObject meta, String name) {
			return meta.findAttribute(name, true);
		}
	};

	private ArrayList<MetaDataObject> subTypes = new ArrayList<>();

	private MetaDataObject superType;

	private HashMap<String, MetaAttributeImpl> attrMap = new HashMap<>();

	private List<MetaAttribute> attrs = new ArrayList<>();

	private List<MetaAttribute> declaredAttrs = new ArrayList<>();

	@SuppressWarnings("unchecked")
	private List<MetaDataObject>[] subTypesCache = new List[4];

	private MetaKey primaryKey;

	private Set<MetaKey> keys = new HashSet<>();

	public MetaDataObjectImpl(Class<?> implClass, Type implType, MetaDataObjectImpl superType) {
		super(null, implClass, implType);
		this.superType = superType;
		if (superType != null) {
			attrs.addAll(superType.getAttributes());
			attrMap.putAll(superType.attrMap);

			superType.addSubType(this);
		}

		initAttributes();
	}

	protected void initAttributes() {
		Class<?> implClass = this.getImplementationClass();
		PropertyUtilsBean utils = BeanUtilsBean.getInstance().getPropertyUtils();
		PropertyDescriptor[] descriptors = utils.getPropertyDescriptors(implClass);
		for (PropertyDescriptor desc : descriptors) {
			if (desc.getReadMethod().getDeclaringClass() != implClass)
				continue; // contained in super type
			if (attrMap.containsKey(desc.getName()))
				throw new IllegalStateException(desc.toString());

			MetaAttributeImpl attr = newAttributeAttribute(this, desc);
			addAttribute(attr);
		}
	}

	protected void addAttribute(MetaAttributeImpl attr) {
		if (attrMap.containsKey(attr.getName()))
			throw new IllegalStateException(attr.toString());

		attrMap.put(attr.getName(), attr);
		attrs.add(attr);
		declaredAttrs.add(attr);
	}

	@Override
	public MetaAttribute getVersionAttribute() {
		for (MetaAttribute attr : getAttributes()) {
			if (attr.isVersion())
				return attr;
		}
		return null;
	}

	private void addSubType(MetaDataObjectImpl subType) {
		this.subTypes.add(subType);
		this.clearCache();
	}

	@SuppressWarnings("unchecked")
	private void clearCache() {
		subTypesCache = new List[4];
	}

	@Override
	public List<MetaAttribute> getAttributes() {
		return attrs;
	}

	@Override
	public List<MetaAttribute> getDeclaredAttributes() {
		return declaredAttrs;
	}

	@Override
	public MetaAttribute getAttribute(String name) {
		MetaAttributeImpl attr = attrMap.get(name);
		PreconditionUtil.assertNotNull(getName() + "." + name, attr);
		return attr;
	}

	protected MetaAttributeImpl newAttributeAttribute(MetaDataObjectImpl metaDataObject, PropertyDescriptor desc) {
		return new MetaAttributeImpl(metaDataObject, desc);
	}

	@Override
	public MetaAttributePath resolvePath(List<String> attrPath, boolean includeSubTypes) {
		MetaAttributeFinder finder = includeSubTypes ? SUBTYPE_ATTRIBUTE_FINDER : DEFAULT_ATTRIBUTE_FINDER;
		return resolvePath(attrPath, finder);
	}

	@Override
	public MetaAttributePath resolvePath(List<String> attrPath) {
		return resolvePath(attrPath, true);
	}

	@Override
	public MetaAttributePath resolvePath(List<String> attrPath, MetaAttributeFinder finder) {
		if (attrPath == null || attrPath.isEmpty())
			throw new IllegalArgumentException("invalid attribute path '" + attrPath + "'");
		LinkedList<MetaAttribute> list = new LinkedList<>();

		MetaDataObject currentMdo = this;
		int i = 0;
		while (i < attrPath.size()) {
			String pathElementName = attrPath.get(i);
			MetaAttribute pathElement = finder.getAttribute(currentMdo, pathElementName);
			if (i < attrPath.size() - 1 && pathElement.getType() instanceof MetaMapType) {
				MetaMapType mapType = (MetaMapType) pathElement.getType();

				// next "attribute" is the key within the map
				String keyString = attrPath.get(i + 1);

				MetaMapAttribute keyAttr = new MetaMapAttributeImpl(mapType, pathElement, keyString);
				list.add(keyAttr);
				i++;
				MetaType valueType = mapType.getValueType();
				currentMdo = nextPathElement(valueType, i, attrPath);
			}
			else {
				list.add(pathElement);
				currentMdo = nextPathElement(pathElement.getType(), i, attrPath);
			}
			i++;
		}

		return new MetaAttributePath(list);
	}

	private MetaDataObject nextPathElement(MetaType pathElementType, int i, List<String> pathElements) {
		if (i == pathElements.size() - 1) {
			return null;
		}
		else {
			if (!(pathElementType instanceof MetaDataObject)) {
				throw new IllegalArgumentException("failed to resolve path " + pathElements);
			}
			return pathElementType.asDataObject();
		}
	}

	@Override
	public MetaAttribute findAttribute(String name, boolean includeSubTypes) {
		if (hasAttribute(name)) {
			return getAttribute(name);
		}

		if (includeSubTypes) {
			List<? extends MetaDataObject> transitiveSubTypes = getSubTypes(true, true);
			for (MetaDataObject subType : transitiveSubTypes) {
				if (subType.hasAttribute(name)) {
					return subType.getAttribute(name);
				}
			}
		}

		throw new IllegalStateException("attribute " + name + " not found in " + getName());
	}

	@Override
	public boolean hasAttribute(String name) {
		return attrMap.containsKey(name);
	}

	@Override
	public MetaDataObject getSuperType() {
		return superType;
	}

	@Override
	public List<MetaDataObject> getSubTypes(boolean transitive, boolean self) {
		int cacheIndex = (transitive ? 2 : 0) | (self ? 1 : 0);

		List<MetaDataObject> cached = subTypesCache[cacheIndex];
		if (cached != null) {
			return cached;
		}
		else {
			ArrayList<MetaDataObject> types = computeSubTypes(transitive, self);
			List<MetaDataObject> unmodifiableList = Collections.unmodifiableList(types);
			subTypesCache[cacheIndex] = unmodifiableList;
			return unmodifiableList;
		}
	}

	private ArrayList<MetaDataObject> computeSubTypes(boolean transitive, boolean self) {
		ArrayList<MetaDataObject> types = new ArrayList<>();

		if (self && (!isAbstract() || !subTypes.isEmpty()))
			types.add(this);

		for (MetaDataObject subType : subTypes) {
			if (!subType.isAbstract() || !subType.getSubTypes().isEmpty())
				types.add(subType);
			if (transitive) {
				types.addAll(subType.getSubTypes(true, false));
			}
		}
		return types;
	}

	@Override
	public boolean isAbstract() {
		return Modifier.isAbstract(getImplementationClass().getModifiers());
	}

	@Override
	public List<MetaDataObject> getSubTypes() {
		return subTypes;
	}

	@Override
	public MetaKey getPrimaryKey() {
		if (primaryKey == null && superType != null) {
			return superType.getPrimaryKey();
		}
		return primaryKey;
	}

	@Override
	public Set<MetaKey> getKeys() {
		return keys;
	}

	public void setPrimaryKey(MetaKey key) {
		this.primaryKey = key;
		addKey(key);
	}

	public void addKey(MetaKey key) {
		keys.add(key);
	}

}

package io.katharsis.jpa.internal.meta.impl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;

import io.katharsis.jpa.internal.meta.MetaAttribute;
import io.katharsis.jpa.internal.meta.MetaAttributePath;
import io.katharsis.jpa.internal.meta.MetaAttributeProjection;
import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.internal.meta.MetaMapAttribute;
import io.katharsis.jpa.internal.meta.MetaMapType;
import io.katharsis.jpa.internal.meta.MetaProjection;
import io.katharsis.jpa.internal.meta.MetaType;
import io.katharsis.jpa.internal.util.KatharsisAssert;

public abstract class MetaDataObjectImpl extends MetaTypeImpl implements MetaDataObject {

	private ArrayList<MetaDataObject> subTypes = new ArrayList<MetaDataObject>();
	private MetaDataObject superType;

	private HashMap<String, MetaAttributeImpl> attrMap = new HashMap<String, MetaAttributeImpl>();
	private List<MetaAttributeImpl> attrs = new ArrayList<MetaAttributeImpl>();
	private List<MetaAttributeImpl> declaredAttrs = new ArrayList<MetaAttributeImpl>();

	@SuppressWarnings("unchecked")
	private transient List<? extends MetaDataObject>[] subTypesCache = new List[4];
	private transient HashMap<String, MetaDataObject> subTypesMapCache;

	public MetaDataObjectImpl(Class<?> implClass, Type implType, MetaDataObjectImpl superType) {
		super(null, implClass, implType);

		this.superType = superType;
		if (superType != null) {
			attrs.addAll(superType.getAttributes());
			attrMap.putAll(superType.attrMap);

			superType.addSubType(this);
		}

		PropertyUtilsBean utils = BeanUtilsBean.getInstance().getPropertyUtils();
		PropertyDescriptor[] descriptors = utils.getPropertyDescriptors(implClass);
		for (PropertyDescriptor desc : descriptors) {
			if (desc.getReadMethod().getDeclaringClass() != implClass)
				continue; // contained in super type
			if (attrMap.containsKey(desc.getName()))
				throw new IllegalStateException(desc.toString());

			MetaAttributeImpl attr = newAttributeAttribute(this, desc);

			attrMap.put(desc.getName(), attr);
			attrs.add(attr);
			declaredAttrs.add(attr);
		}
	}

	@Override
	public MetaAttribute getVersionAttribute() {
		for (MetaAttributeImpl attr : getAttributes()) {
			if (attr.isVersion())
				return attr;
		}
		return null;
	}

	private void addSubType(MetaDataObjectImpl subType) {
		this.subTypes.add(subType);
		this.clearCache();
	}

	private void clearCache() {
		subTypesMapCache = null;
		subTypesCache = new List[4];
	}

	@Override
	public List<MetaAttributeImpl> getAttributes() {
		return attrs;
	}

	@Override
	public List<MetaAttributeImpl> getDeclaredAttributes() {
		return declaredAttrs;
	}

	public MetaAttribute getAttribute(String name) {
		MetaAttributeImpl attr = attrMap.get(name);
		KatharsisAssert.assertNotNull(getName() + "." + name, attr);
		return attr;
	}

	public String toString(Object entity) {
		boolean notFirst = false;
		StringBuilder b = new StringBuilder(entity.getClass().getSimpleName());
		b.append('[');

		for (MetaAttributeImpl attr : getAttributes()) {
			// FIXME remo DECIDE: should be print one-relation as well? would
			// probably require access to HibernateProxy,
			// etc. to prevent lazy
			// loading
			if (attr.isAssociation())
				continue;

			Object value = attr.getValue(entity);

			if (notFirst) {
				b.append(',');
			} else {
				notFirst = true;
			}
			b.append(attr.getName());
			b.append("=");
			b.append(formatValue(value));
		}
		b.append(']');
		return b.toString();
	}

	private Object formatValue(Object value) {
		if (value == null) {
			return "null";
		} else if (value instanceof Calendar) {
			Calendar cal = (Calendar) value;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss Z");
			return (sdf.format(cal.getTime()));
		} else if (value instanceof Date) {
			Date cal = (Date) value;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss Z");
			return (sdf.format(cal.getTime()));
		} else {
			return value.toString();
		}
	}

	protected abstract MetaAttributeImpl newAttributeAttribute(MetaDataObjectImpl metaDataObject,
			PropertyDescriptor desc);

	@Override
	public MetaAttributePath resolvePath(String attrPath, boolean includeSubTypes) {
		LinkedList<? extends MetaAttribute> elements = resolveAttributePath(attrPath, includeSubTypes);
		return new MetaAttributePath(elements);
	}

	@Override
	public MetaAttributePath resolvePath(String attrPath) {
		LinkedList<? extends MetaAttribute> elements = resolveAttributePath(attrPath, true);
		return new MetaAttributePath(elements);
	}

	public LinkedList<? extends MetaAttribute> resolveAttributePath(String attrPath, boolean includeSubTypes) {
		if (attrPath == null || attrPath.length() == 0)
			throw new IllegalArgumentException("invalid attribute path '" + attrPath + "'");
		LinkedList<MetaAttribute> list = new LinkedList<MetaAttribute>();
		if (attrPath.contains(".")) {
			// dots in path, we need to dive down into the entity hierarchy
			MetaDataObject currentMdo = this;
			String[] attributeNames = attrPath.split("\\.");
			for (int i = 0; i < attributeNames.length; i++) {
				String attrName = attributeNames[i];
				MetaAttribute currentAttr = ((MetaDataObjectImpl) currentMdo).findAttribute(attrName, includeSubTypes);

				if (currentAttr.getType() instanceof MetaMapType) {
					MetaMapType mapType = (MetaMapType) currentAttr.getType();
					if (MetaUtils.isMapKeyAttribute(attrName)) {
						MetaMapAttribute keyAttr;
						if (currentAttr instanceof MetaAttributeProjection)
							keyAttr = new MetaMapAttributeProjectionImpl(mapType, (MetaAttributeProjection) currentAttr,
									null, false);
						else
							keyAttr = new MetaMapAttributeImpl(mapType, currentAttr, null, false);
						// ((MetaElement) keyAttr).setParent((MetaElement)
						// currentAttr.getParent());
						list.add(keyAttr);
					} else if (i < attributeNames.length - 1) {
						// next "attribute" will be a key of a map
						String keyString = attributeNames[i + 1];

						MetaMapAttribute keyAttr;
						if (currentAttr instanceof MetaAttributeProjection)
							keyAttr = new MetaMapAttributeProjectionImpl(mapType, (MetaAttributeProjection) currentAttr,
									keyString, true);
						else
							keyAttr = new MetaMapAttributeImpl(mapType, currentAttr, keyString, true);
						// ((MetaElement) keyAttr).setParent((MetaElement)
						// currentAttr.getParent());
						list.add(keyAttr);
						i++;
						MetaType valueType = mapType.getValueType();

						if (i == attributeNames.length - 1)
							break;
						currentMdo = valueType.asDataObject();
					} else {
						// IMetaMapAttribute keyAttr;
						// if (currentAttr instanceof IMetaAttributeProjection)
						// keyAttr = new MetaMapAttributeProjection(mapType,
						// (IMetaAttributeProjection) currentAttr);
						// else
						// keyAttr = new MetaMapAttribute(mapType, currentAttr);
						// ((MetaElement) keyAttr).setParent((MetaElement)
						// currentAttr.getParent());
						// list.add(keyAttr);
						// currentMdo = mapType.getValueType().asDataObject();
						throw new IllegalStateException("not implemented");
					}
				} else {
					list.add(currentAttr);

					if (i == attributeNames.length - 1)
						break;
					currentMdo = currentAttr.getType().asDataObject();
				}
			}
		} else {
			// no dots in path
			MetaAttribute attr = findAttribute(attrPath, includeSubTypes);
			if (attr != null && MetaUtils.isMapKeyAttribute(attrPath) && attr.getType() instanceof MetaMapType) {
				MetaMapType mapType = (MetaMapType) attr.getType();
				MetaMapAttribute mapAttr;
				if (attr instanceof MetaAttributeProjection) {
					mapAttr = new MetaMapAttributeProjectionImpl(mapType, (MetaAttributeProjection) attr, null, true);
				} else {
					mapAttr = new MetaMapAttributeImpl(mapType, attr, null, true);
				}
				((MetaElementImpl) mapAttr).setParent((MetaElementImpl) attr.getParent());
				attr = mapAttr;
			}
			list.add(attr);
		}
		return list;
	}

	private MetaAttribute findAttribute(String name, boolean includeSubTypes) {
		if (hasAttribute(name))
			return getAttribute(name);

		if (includeSubTypes) {
			List<? extends MetaDataObject> subTypes = getSubTypes(true, true);
			for (MetaDataObject subType : subTypes) {
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

	public MetaDataObject getRootType() {
		if (getSuperType() != null) // ensure proxy resolve
			return superType.getRootType();
		else
			return this;
	}

	@Override
	public List<? extends MetaDataObject> getSubTypes(boolean transitive, boolean self) {
		int cacheIndex = (transitive ? 2 : 0) | (self ? 1 : 0);

		List<? extends MetaDataObject> cached = subTypesCache[cacheIndex];
		if (cached != null)
			return cached;

		ArrayList<MetaDataObject> types = new ArrayList<MetaDataObject>();

		if (self && (!isAbstract() || !subTypes.isEmpty()))
			types.add(this);

		for (MetaDataObjectImpl subType : (Iterable<MetaDataObjectImpl>) (Iterable) subTypes) {
			if (!subType.isAbstract() || !subType.getSubTypes().isEmpty())
				types.add(subType);
			if (transitive) {
				types.addAll(subType.getSubTypes(true, false));
			}
		}

		subTypesCache[cacheIndex] = Collections.unmodifiableList(types);
		return types;
	}

	private boolean isAbstract() {
		return Modifier.isAbstract(getImplementationClass().getModifiers());
	}

	@Override
	public List<? extends MetaDataObject> getSubTypes() {
		return subTypes;
	}

	private boolean isSuperTypeOf(MetaDataObject sub) {
		return this == sub || (sub != null && isSuperTypeOf(sub.getSuperType()));
	}

	@Override
	public MetaDataObject findSubTypeOrSelf(Class<?> implClass) {
		if (implClass == null)
			throw new NullPointerException("class is null");
		Class<?> localImplClass = getImplementationClass();
		if (implClass == localImplClass)
			return this;
		MetaDataObject subType = MetaLookup.INSTANCE.getMeta(implClass).asDataObject();
		if (isSuperTypeOf(subType))
			return subType;
		return null;
	}

	/**
	 * Gets the subtype with the given name (simple or qualitified).
	 */
	public MetaDataObject findSubTypeOrSelf(String name) {
		HashMap<String, MetaDataObject> cache = subTypesMapCache;
		if (cache == null) {
			cache = new HashMap<String, MetaDataObject>();
			List<? extends MetaDataObject> subTypes = getSubTypes(true, true);
			for (MetaDataObject subType : subTypes) {
				cache.put(subType.getName(), subType);
				cache.put(subType.getId(), subType);
			}
			subTypesMapCache = cache;
		}
		return subTypesMapCache.get(name);
	}

	@Override
	public MetaProjection asProjection() {
		if (this instanceof MetaProjection)
			return (MetaProjection) this;
		throw new IllegalStateException("not a projection");
	}
}
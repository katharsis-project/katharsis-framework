package io.katharsis.jpa.internal.query;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaLookup;

public class VirtualAttributeRegistry {

	private Map<String, Registration> map = new HashMap<>();
	private MetaLookup metaLookup;

	private static class Registration {
		private MetaVirtualAttribute attribute;
		private Object expressionFactory;
	}

	public VirtualAttributeRegistry(MetaLookup metaLookup) {
		this.metaLookup = metaLookup;
	}

	public Object get(MetaVirtualAttribute attr) {
		Class<?> clazz = attr.getParent().getImplementationClass();
		Registration registration = map.get(key(clazz, attr.getName()));
		return registration != null ? registration.expressionFactory : null;
	}

	public MetaVirtualAttribute get(MetaDataObject meta, String name) {
		Class<?> clazz = meta.getImplementationClass();
		Registration registration = map.get(key(clazz, name));
		return registration != null ? registration.attribute : null;
	}

	public void register(Class<?> targetClass, String name, Object expressionFactory, Type type) {
		MetaDataObject targetMeta = metaLookup.getMeta(targetClass).asDataObject();
		MetaVirtualAttribute attr = new MetaVirtualAttribute(targetMeta, name, type);
		attr.init(metaLookup);

		Registration registration = new Registration();
		registration.attribute = attr;
		registration.expressionFactory = expressionFactory;
		map.put(key(targetClass, name), registration);
	}

	private String key(Class<?> targetClass, String name) {
		return targetClass.getName() + "." + name;
	}
}

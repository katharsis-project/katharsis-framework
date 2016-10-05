package io.katharsis.jpa.internal.query;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.katharsis.jpa.internal.meta.MetaDataObject;
import io.katharsis.jpa.internal.meta.MetaLookup;
import io.katharsis.jpa.query.ComputedAttributeRegistry;

public class ComputedAttributeRegistryImpl implements ComputedAttributeRegistry {

	private Map<String, Registration> map = new HashMap<>();

	private MetaLookup metaLookup;

	private static class Registration {

		private MetaComputedAttribute attribute;

		private Object expressionFactory;
	}

	public ComputedAttributeRegistryImpl(MetaLookup metaLookup) {
		this.metaLookup = metaLookup;
	}

	public Object get(MetaComputedAttribute attr) {
		Class<?> clazz = attr.getParent().getImplementationClass();
		Registration registration = map.get(key(clazz, attr.getName()));
		return registration != null ? registration.expressionFactory : null;
	}

	public MetaComputedAttribute get(MetaDataObject meta, String name) {
		Class<?> clazz = meta.getImplementationClass();
		Registration registration = map.get(key(clazz, name));
		return registration != null ? registration.attribute : null;
	}

	public void register(Class<?> targetClass, String name, Object expressionFactory, Type type) {
		MetaDataObject targetMeta = metaLookup.getMeta(targetClass).asDataObject();
		MetaComputedAttribute attr = new MetaComputedAttribute(targetMeta, name, type);
		attr.init(metaLookup);

		Registration registration = new Registration();
		registration.attribute = attr;
		registration.expressionFactory = expressionFactory;
		map.put(key(targetClass, name), registration);
	}

	private String key(Class<?> targetClass, String name) {
		return targetClass.getName() + "." + name;
	}

	@Override
	public Set<String> getForType(Class<?> entityType) {
		Set<String> set = new HashSet<>();
		for (Registration reg : map.values()) {
			MetaDataObject parent = reg.attribute.getParent();
			Class<?> parentImpl = parent.getImplementationClass();
			if (parentImpl.isAssignableFrom(entityType)) {
				set.add(reg.attribute.getName());
			}
		}
		return set;
	}
}

package io.katharsis.meta;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.katharsis.core.internal.utils.MultivaluedMap;
import io.katharsis.core.internal.utils.PreconditionUtil;
import io.katharsis.meta.model.MetaArrayType;
import io.katharsis.meta.model.MetaElement;
import io.katharsis.meta.model.MetaEnumType;
import io.katharsis.meta.model.MetaListType;
import io.katharsis.meta.model.MetaLiteral;
import io.katharsis.meta.model.MetaMapType;
import io.katharsis.meta.model.MetaPrimitiveType;
import io.katharsis.meta.model.MetaSetType;
import io.katharsis.meta.model.MetaType;
import io.katharsis.meta.provider.MetaProvider;
import io.katharsis.meta.provider.MetaProviderContext;

public class MetaLookup {

	private static final Logger LOGGER = LoggerFactory.getLogger(MetaLookup.class);

	private static final String BASE_ID_PREFIX = "base.";

	private MultivaluedMap<Type, MetaElement> typeElementsMap = new MultivaluedMap<>();

	private ConcurrentHashMap<String, MetaElement> idElementMap = new ConcurrentHashMap<>();

	private Set<Class<?>> primitiveTypes = Collections.newSetFromMap(new ConcurrentHashMap<Class<?>, Boolean>());

	private List<MetaProvider> providers = new CopyOnWriteArrayList<>();

	private LinkedList<MetaElement> initializationQueue = new LinkedList<>();

	private boolean adding = false;

	private MetaProviderContext context;

	private Map<String, String> packageIdMapping = new HashMap<>();

	private boolean discovered;

	public MetaLookup() {
		registerPrimitiveType(String.class);
		registerPrimitiveType(Number.class);
		registerPrimitiveType(Boolean.class);
		registerPrimitiveType(UUID.class);
		registerPrimitiveType(Date.class);
		registerPrimitiveType(Timestamp.class);
		registerPrimitiveType(byte[].class);
		registerPrimitiveType(boolean[].class);
		registerPrimitiveType(int[].class);
		registerPrimitiveType(short[].class);
		registerPrimitiveType(long[].class);
		registerPrimitiveType(double[].class);
		registerPrimitiveType(float[].class);

		context = new MetaProviderContext() {

			@Override
			public void add(MetaElement element) {
				MetaLookup.this.add(element);
			}

			@Override
			public void addAll(Collection<? extends MetaElement> elements) {
				MetaLookup.this.addAll(elements);
			}

			@Override
			public MetaLookup getLookup() {
				return MetaLookup.this;
			}
		};

		putIdMapping("io.katharsis.jpa.meta", "io.katharsis.jpa");
		putIdMapping("io.katharsis.meta.model", "io.katharsis.meta");
		putIdMapping("io.katharsis.meta.model.resource", "io.katharsis.meta.resource");
	}

	public Map<String, MetaElement> getMetaById() {
		return Collections.unmodifiableMap(idElementMap);
	}

	public void registerPrimitiveType(Class<?> clazz) {
		primitiveTypes.add(clazz);
	}

	public void addProvider(MetaProvider provider) {
		if (!providers.contains(provider)) {
			providers.add(provider);
			for (MetaProvider dependency : provider.getDependencies()) {
				addProvider(dependency);
			}
		}
	}

	public MetaElement getMeta(Type type) {
		return getMeta(type, MetaElement.class);
	}

	public <T extends MetaElement> T getMeta(Type type, Class<T> metaClass) {
		return (T) getMetaInternal(type, metaClass, false);
	}

	public <T extends MetaElement> T getMeta(Type type, Class<T> metaClass, boolean nullable) {
		return (T) getMetaInternal(type, metaClass, nullable);
	}

	public MetaArrayType getArrayMeta(Type type, Class<? extends MetaElement> elementMetaClass) {
		return (MetaArrayType) getMetaInternal(type, elementMetaClass, false);
	}

	private MetaElement getMetaInternal(Type type, Class<? extends MetaElement> elementMetaClass, boolean nullable) {
		PreconditionUtil.assertNotNull("type must not be null", type);

		checkInitialized();

		MetaElement meta = getUniqueElementByType(type, elementMetaClass);
		if (meta == null) {
			synchronized (this) {
				meta = getUniqueElementByType(type, elementMetaClass);
				if (meta == null) {

					boolean wasInitializing = adding;
					if (!wasInitializing) {
						adding = true;
					}

					meta = allocateMeta(type, elementMetaClass, nullable);
					if (meta != null) {
						add(meta);
					}

					if (!wasInitializing) {
						initialize();
					}
				}
			}
		}
		return meta;
	}

	private MetaElement getUniqueElementByType(Type type, Class<? extends MetaElement> elementMetaClass) {
		if (!typeElementsMap.containsKey(type)) {
			return null;
		}
		List<MetaElement> elements = typeElementsMap.getList(type);
		MetaElement result = null;
		for (MetaElement element : elements) {
			MetaElement meta = element;
			if (meta instanceof MetaType) {
				meta = ((MetaType) meta).getElementType();
			}

			if (elementMetaClass.isInstance(meta)) {
				if (result != null) {
					throw new IllegalStateException(
							"multiple elements found of type " + elementMetaClass + ": " + result + " vs " + element);
				}
				result = element;
			}
		}
		return result;
	}

	private MetaElement allocateMeta(Type type, Class<? extends MetaElement> metaClass, boolean nullable) {
		LOGGER.debug("allocate {}", type);

		if (type instanceof Class) {
			MetaElement meta = allocateMetaFromClass(type, metaClass);
			if (meta != null) {
				return meta;
			}
		}

		if (type instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType) type;
			return allocateMetaFromParamerizedType(paramType, metaClass);
		}

		MetaElement meta = allocateMetaFromFactory(type, metaClass);
		if (meta != null) {
			return meta;
		}

		if (nullable) {
			return null;
		}
		throw new UnsupportedOperationException("unknown type " + type);
	}

	private MetaElement allocateMetaFromClass(Type type, Class<? extends MetaElement> metaClass) {
		Class<?> clazz = (Class<?>) type;
		if (clazz.isEnum()) {
			MetaEnumType enumType = new MetaEnumType();
			enumType.setImplementationType(type);
			enumType.setName(clazz.getSimpleName());
			for (Object literalObj : clazz.getEnumConstants()) {
				MetaLiteral literal = new MetaLiteral();
				literal.setName(literalObj.toString());
				literal.setParent(enumType);
			}
			return enumType;
		}
		if (isPrimitiveType(clazz)) {
			// FIXME should long + Long really be merged???
			String id = BASE_ID_PREFIX + clazz.getSimpleName().toLowerCase();

			MetaPrimitiveType primitiveType = (MetaPrimitiveType) idElementMap.get(id);
			if (primitiveType == null) {
				primitiveType = new MetaPrimitiveType();
				primitiveType.setImplementationType(type);
				primitiveType.setName(clazz.getSimpleName().toLowerCase());
				primitiveType.setId(id);
			}
			return primitiveType;
		}
		else if (clazz.isArray()) {
			Class<?> elementClass = ((Class<?>) type).getComponentType();

			MetaType elementType = getMeta(elementClass, metaClass).asType();
			MetaArrayType arrayType = new MetaArrayType();
			arrayType.setName(elementType.getName() + "[]");
			arrayType.setImplementationType(type);
			arrayType.setElementType(elementType);
			return arrayType;
		}
		return null;
	}

	private <T extends MetaElement> T allocateMetaFromFactory(Type type, Class<? extends MetaElement> metaClass) {
		for (MetaProvider factory : providers) {
			if (factory.accept(type, metaClass)) {
				return (T) factory.createElement(type, context);
			}
		}
		return null;
	}

	private MetaElement allocateMetaFromParamerizedType(ParameterizedType paramType,
			Class<? extends MetaElement> elementMetaClass) {
		if (paramType.getRawType() instanceof Class && Map.class.isAssignableFrom((Class<?>) paramType.getRawType())) {
			PreconditionUtil.assertEquals("expected 2 type arguments", 2, paramType.getActualTypeArguments().length);
			MetaType keyType = getMeta(paramType.getActualTypeArguments()[0]).asType();
			MetaType valueType = getMeta(paramType.getActualTypeArguments()[1], elementMetaClass).asType();
			MetaMapType mapMeta = new MetaMapType();

			boolean primitiveKey = keyType instanceof MetaPrimitiveType;
			boolean primitiveValue = valueType instanceof MetaPrimitiveType;
			if (primitiveKey || !primitiveValue) {
				mapMeta.setName(valueType.getName() + "$MappedBy$" + keyType.getName());
				mapMeta.setId(valueType.getId() + "$MappedBy$" + keyType.getName());
			}
			else {
				mapMeta.setName(keyType.getName() + "$Map$" + valueType.getName());
				mapMeta.setId(keyType.getId() + "$Map$" + valueType.getName());
			}

			mapMeta.setImplementationType(paramType);
			mapMeta.setKeyType(keyType);
			mapMeta.setValueType(valueType);
			return mapMeta;
		}
		else if (paramType.getRawType() instanceof Class
				&& Collection.class.isAssignableFrom((Class<?>) paramType.getRawType())) {
			return allocateMetaFromCollectionType(paramType, elementMetaClass);
		}
		else {
			throw new UnsupportedOperationException("unknown type " + paramType);
		}
	}

	private MetaElement allocateMetaFromCollectionType(ParameterizedType paramType,
			Class<? extends MetaElement> elementMetaClass) {
		PreconditionUtil.assertEquals("expected single type argument", 1, paramType.getActualTypeArguments().length);
		MetaType elementType = getMeta(paramType.getActualTypeArguments()[0], elementMetaClass).asType();

		boolean isSet = Set.class.isAssignableFrom((Class<?>) paramType.getRawType());
		boolean isList = List.class.isAssignableFrom((Class<?>) paramType.getRawType());
		if (isSet) {
			MetaSetType metaSet = new MetaSetType();
			metaSet.setId(elementType.getId() + "$Set");
			metaSet.setName(elementType.getName() + "$Set");
			metaSet.setImplementationType(paramType);
			metaSet.setElementType(elementType);
			return metaSet;
		}
		else if (isList) {
			MetaListType metaList = new MetaListType();
			metaList.setId(elementType.getId() + "$List");
			metaList.setName(elementType.getName() + "$List");
			metaList.setImplementationType(paramType);
			metaList.setElementType(elementType);
			return metaList;
		}
		else {
			throw new IllegalStateException("expected list or set type: " + paramType.toString());
		}
	}

	private boolean isPrimitiveType(Class<?> clazz) {
		if (clazz == Object.class) {
			return true;
		}

		if(clazz.getPackage() != null && clazz.getPackage().getName().equals("java.time")){
			return true;
		}
		
		if (clazz.isPrimitive() || primitiveTypes.contains(clazz))
			return true;

		for (Class<?> primitiveType : primitiveTypes) {
			if (primitiveType.isAssignableFrom(clazz)) {
				return true;
			}
		}
		return false;
	}

	public static Class<?> getRawType(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		}
		else if (type instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType) type;
			return getRawType(paramType.getRawType());
		}
		else {
			throw new IllegalArgumentException("unable to obtain raw type for " + type);
		}
	}

	public void addAll(Collection<? extends MetaElement> elements) {
		for (MetaElement element : elements) {
			add(element);
		}
	}

	public void add(MetaElement element) {
		PreconditionUtil.assertNotNull("no name provided", element.getName());
		if (element instanceof MetaType) {
			MetaType typeElement = element.asType();
			Class<?> implClass = typeElement.getImplementationClass();
			if (!element.hasId()) {
				element.setId(computeIdPrefixFromPackage(implClass, element) + element.getName());
			}
		}

		if (!element.hasId() && element.getParent() != null) {
			element.setId(element.getParent().getId() + "." + element.getName());
		}

		if (idElementMap.get(element.getId()) != element) {
			LOGGER.debug("add {} of type {}", element.getId(), element.getClass().getSimpleName());

			// queue for initialization
			initializationQueue.add(element);

			// add to data structures
			if (element instanceof MetaType) {
				MetaType typeElement = element.asType();

				// check not alreay exists
				if (typeElementsMap.containsKey(typeElement.getImplementationType())) {
					List<MetaElement> existingElements = typeElementsMap.getList(typeElement.getImplementationType());
					for (MetaElement existingElement : existingElements) {
						if( existingElement.getId().equals(element.getId())){
							throw new IllegalStateException(element.getId() + " already available: " + existingElement + " vs " + element );
						}
					}
				}

				typeElementsMap.add(typeElement.getImplementationType(), element);
			}
			MetaElement currentElement = idElementMap.get(element.getId());
			PreconditionUtil.assertNull(element.getId(), currentElement);
			idElementMap.put(element.getId(), element);

			// add children recursively
			for (MetaElement child : element.getChildren()) {
				add(child);
			}
		}
	}

	private String computeIdPrefixFromPackage(Class<?> implClass, MetaElement element) {
		Package implPackage = implClass.getPackage();
		if (implPackage == null && implClass.isArray()) {
			implPackage = implClass.getComponentType().getPackage();
		}
		if (implPackage == null) {
			throw new IllegalStateException(implClass.getName() + " does not belong to a package");
		}
		String packageName = implPackage.getName();
		StringBuilder idInfix = new StringBuilder(".");
		while (true) {

			String idMappingKey1 = toIdMappingKey(packageName, element.getClass());
			String idMappingKey2 = toIdMappingKey(packageName, null);

			String idPrefix = packageIdMapping.get(idMappingKey1);
			if (idPrefix == null) {
				idPrefix = packageIdMapping.get(idMappingKey2);
			}
			if (idPrefix != null) {
				return idPrefix + idInfix;
			}
			int sep = packageName.lastIndexOf('.');
			if (sep == -1) {
				break;
			}
			idInfix.append(packageName.substring(sep + 1));
			idInfix.append(".");
			packageName = packageName.substring(0, sep);
		}
		return implPackage.getName() + ".";
	}

	private void checkInitialized() {
		if (!discovered && !adding) {
			initialize();
		}
	}

	public void initialize() {
		LOGGER.debug("adding");
		adding = true;
		try {
			if (!discovered) {
				for (MetaProvider provider : providers) {
					provider.discoverElements(context);
				}
				discovered = true;
			}

			while (!initializationQueue.isEmpty()) {
				MetaElement element = initializationQueue.pollFirst();
				// initialize from roots down to decendants.
				if (element.getParent() == null) {
					initialize(element);
				}
			}
		}
		finally {
			LOGGER.debug("added");
			adding = false;
		}
	}

	private void initialize(MetaElement element) {
		LOGGER.debug("adding {}", element.getId());
		for (MetaProvider initializer : providers) {
			packageIdMapping.putAll(initializer.getIdMappings());
		}

		for (MetaProvider initializer : providers) {
			initializer.onInitializing(context, element);
		}

		for (MetaElement child : element.getChildren()) {
			initialize(child);
		}

		for (MetaProvider initializer : providers) {
			initializer.onInitialized(context, element);
		}
		LOGGER.debug("added {}", element.getId());
	}

	public List<MetaProvider> getProviders() {
		return providers;
	}

	public void putIdMapping(String packageName, String idPrefix) {
		packageIdMapping.put(packageName, idPrefix);
	}

	public void putIdMapping(String packageName, Class<? extends MetaElement> type, String idPrefix) {
		packageIdMapping.put(toIdMappingKey(packageName, type), idPrefix);
	}

	private String toIdMappingKey(String packageName, Class<? extends MetaElement> type) {
		if (type != null) {
			return packageName + "#" + type.getName();
		}
		else {
			return packageName;
		}
	}
}

package io.katharsis.jpa.internal.meta;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import io.katharsis.jpa.internal.meta.impl.MetaArrayTypeImpl;
import io.katharsis.jpa.internal.meta.impl.MetaDataObjectImpl;
import io.katharsis.jpa.internal.meta.impl.MetaElementImpl;
import io.katharsis.jpa.internal.meta.impl.MetaEmbeddableImpl;
import io.katharsis.jpa.internal.meta.impl.MetaEntityImpl;
import io.katharsis.jpa.internal.meta.impl.MetaListTypeImpl;
import io.katharsis.jpa.internal.meta.impl.MetaMapTypeImpl;
import io.katharsis.jpa.internal.meta.impl.MetaMappedSuperclassImpl;
import io.katharsis.jpa.internal.meta.impl.MetaPrimitiveType;
import io.katharsis.jpa.internal.meta.impl.MetaResourceImpl;
import io.katharsis.jpa.internal.meta.impl.MetaSetTypeImpl;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.utils.PreconditionUtil;

public class MetaLookup {

	private ConcurrentHashMap<Type, MetaElement> cache = new ConcurrentHashMap<>();

	private Set<Class<?>> primitiveTypes = Collections.newSetFromMap(new ConcurrentHashMap<Class<?>, Boolean>());
	private List<AnnotationMetaElementFactory> factories = new CopyOnWriteArrayList<>();

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

		registerFactory(new EntityFactory());
		registerFactory(new EmbeddableFactory());
		registerFactory(new MappedSuperclassFactory());
		registerFactory(new JsonApiResourceFactory());
	}

	public void registerPrimitiveType(Class<?> clazz) {
		primitiveTypes.add(clazz);
	}

	public void registerFactory(AnnotationMetaElementFactory factory) {
		factories.add(factory);
	}

	public MetaElement getMeta(Type type) {
		if (type == null) {
			throw new IllegalArgumentException("type must not be null");
		}
		MetaElement meta = cache.get(type);
		if (meta == null) {
			synchronized (this) {
				meta = cache.get(type);
				if (meta == null) {
					meta = allocateMeta(type);

					cache.put(type, meta);
					((MetaElementImpl) meta).init(this);
				}
			}
		}
		return meta;
	}

	private MetaElement allocateMeta(Type type) {
		MetaElement meta = allocateMetaFromFactory(type);
		if (meta != null) {
			return meta;
		}

		if (type instanceof Class) {
			return allocateMetaFromClass(type);
		} else if (type instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType) type;
			return allocateMetaFromParamerizedType(paramType);
		} else {
			throw new UnsupportedOperationException("unknown type " + type);
		}
	}

	private MetaElement allocateMetaFromClass(Type type) {
		Class<?> clazz = (Class<?>) type;
		if (isPrimitiveType(clazz)) {
			return new MetaPrimitiveType(clazz, type);
		} else if (clazz.isArray()) {
			Class<?> elementClass = ((Class<?>) type).getComponentType();

			MetaType elementType = getMeta(elementClass).asType();
			return new MetaArrayTypeImpl(null, (Class<?>) type, type, elementType);
		} else {
			Class<?> superClazz = clazz.getSuperclass();
			MetaElement superMeta = null;
			if (superClazz != Object.class && superClazz != null) {
				superMeta = getMeta(superClazz);
			}
			return new MetaDataObjectImpl((Class<?>) type, type, (MetaDataObjectImpl) superMeta);
		}
	}

	private MetaElement allocateMetaFromFactory(Type type) {
		for (AnnotationMetaElementFactory factory : factories) {
			Class<? extends Annotation> annotationClass = factory.getAnnotation();
			Class<?> clazz = getRawType(type);
			Annotation annotation = clazz.getAnnotation(annotationClass);
			if (annotation != null) {
				return factory.create(type, this);
			}
		}
		return null;
	}

	private MetaElement allocateMetaFromParamerizedType(ParameterizedType paramType) {
		if (paramType.getRawType() instanceof Class && Map.class.isAssignableFrom((Class<?>) paramType.getRawType())) {
			PreconditionUtil.assertEquals(2, paramType.getActualTypeArguments().length);
			MetaType keyType = getMeta(paramType.getActualTypeArguments()[0]).asType();
			MetaType valueType = getMeta(paramType.getActualTypeArguments()[1]).asType();
			return new MetaMapTypeImpl(null, (Class<?>) paramType.getRawType(), paramType, keyType, valueType);
		} else if (paramType.getRawType() instanceof Class
				&& Collection.class.isAssignableFrom((Class<?>) paramType.getRawType())) {
			return allocateMetaFromCollectionType(paramType);
		} else {
			throw new UnsupportedOperationException("unknown type " + paramType);
		}
	}

	private MetaElement allocateMetaFromCollectionType(ParameterizedType paramType) {
		PreconditionUtil.assertEquals(1, paramType.getActualTypeArguments().length);
		MetaType elementType = getMeta(paramType.getActualTypeArguments()[0]).asType();

		boolean isSet = Set.class.isAssignableFrom((Class<?>) paramType.getRawType());
		boolean isList = List.class.isAssignableFrom((Class<?>) paramType.getRawType());
		if (isSet) {
			return new MetaSetTypeImpl(null, (Class<?>) paramType.getRawType(), paramType, elementType);
		} else if (isList) {
			return new MetaListTypeImpl(null, (Class<?>) paramType.getRawType(), paramType, elementType);
		} else {
			throw new IllegalStateException("expected list or set type: " + paramType.toString());
		}
	}

	private boolean isPrimitiveType(Class<?> clazz) {
		if (clazz.isPrimitive() || primitiveTypes.contains(clazz))
			return true;

		for (Class<?> primitiveType : primitiveTypes) {
			if (primitiveType.isAssignableFrom(clazz)) {
				return true;
			}
		}
		return false;
	}

	static class EntityFactory implements AnnotationMetaElementFactory {

		@Override
		public Class<? extends Annotation> getAnnotation() {
			return Entity.class;
		}

		@Override
		public MetaElement create(Type type, MetaLookup lookup) {
			Class<?> rawClazz = getRawType(type);
			Class<?> superClazz = rawClazz.getSuperclass();
			MetaElement superMeta = null;
			if (superClazz != Object.class) {
				superMeta = lookup.getMeta(superClazz);
			}
			return new MetaEntityImpl(rawClazz, type, (MetaDataObjectImpl) superMeta);
		}
	}

	static class EmbeddableFactory implements AnnotationMetaElementFactory {

		@Override
		public Class<? extends Annotation> getAnnotation() {
			return Embeddable.class;
		}

		@Override
		public MetaElement create(Type type, MetaLookup lookup) {
			Class<?> rawClazz = getRawType(type);
			Class<?> superClazz = rawClazz.getSuperclass();
			MetaElement superMeta = null;
			if (superClazz != Object.class) {
				superMeta = lookup.getMeta(superClazz);
			}
			return new MetaEmbeddableImpl(rawClazz, type, (MetaDataObjectImpl) superMeta);
		}
	}

	static class MappedSuperclassFactory implements AnnotationMetaElementFactory {

		@Override
		public Class<? extends Annotation> getAnnotation() {
			return MappedSuperclass.class;
		}

		@Override
		public MetaElement create(Type type, MetaLookup lookup) {
			Class<?> rawClazz = getRawType(type);
			Class<?> superClazz = rawClazz.getSuperclass();
			MetaElement superMeta = null;
			if (superClazz != Object.class) {
				superMeta = lookup.getMeta(superClazz);
			}
			return new MetaMappedSuperclassImpl(rawClazz, type, (MetaDataObjectImpl) superMeta);
		}
	}

	static class JsonApiResourceFactory implements AnnotationMetaElementFactory {

		@Override
		public Class<? extends Annotation> getAnnotation() {
			return JsonApiResource.class;
		}

		@Override
		public MetaElement create(Type type, MetaLookup lookup) {
			Class<?> rawClazz = getRawType(type);
			Class<?> superClazz = rawClazz.getSuperclass();
			MetaElement superMeta = null;
			if (superClazz != Object.class) {
				superMeta = lookup.getMeta(superClazz);
			}
			return new MetaResourceImpl(rawClazz, type, (MetaDataObjectImpl) superMeta);
		}
	}

	public static Class<?> getRawType(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType) type;
			return getRawType(paramType.getRawType());
		} else {
			throw new IllegalArgumentException("unable to obtain raw type for " + type);
		}
	}
}
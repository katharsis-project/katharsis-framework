package io.katharsis.jpa.internal.meta;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Embeddable;
import javax.persistence.Entity;

import io.katharsis.jpa.internal.meta.impl.MetaCollectionTypeImpl;
import io.katharsis.jpa.internal.meta.impl.MetaDataObjectImpl;
import io.katharsis.jpa.internal.meta.impl.MetaElementImpl;
import io.katharsis.jpa.internal.meta.impl.MetaEmbeddableImpl;
import io.katharsis.jpa.internal.meta.impl.MetaEntityImpl;
import io.katharsis.jpa.internal.meta.impl.MetaMapTypeImpl;
import io.katharsis.jpa.internal.meta.impl.MetaPrimitiveType;
import io.katharsis.jpa.internal.util.KatharsisAssert;

public class MetaLookup {

	public static final MetaLookup INSTANCE = new MetaLookup();

	private ConcurrentHashMap<Type, MetaElement> cache = new ConcurrentHashMap<Type, MetaElement>();

	// public MetaElement getMeta(Class<?> clazz) {
	//
	// }
	public MetaElement getMeta(Type type) {
		MetaElement meta = cache.get(type);
		if (meta == null) {
			synchronized (this) {
				meta = cache.get(type);
				if (meta == null) {

					if (type instanceof Class) {
						Class<?> clazz = (Class<?>) type;

						// TODO support other types
						Entity entityAnnotation = clazz.getAnnotation(Entity.class);
						Embeddable embAnnotation = clazz.getAnnotation(Embeddable.class);
						if (entityAnnotation != null) {
							Class<?> superClazz = clazz.getSuperclass();
							MetaElement superMeta = null;
							if (superClazz != Object.class) {
								superMeta = getMeta(superClazz);
							}
							meta = new MetaEntityImpl(clazz, type, (MetaDataObjectImpl) superMeta);
						} else if (embAnnotation != null) {
							Class<?> superClazz = clazz.getSuperclass();
							MetaElement superMeta = null;
							if (superClazz != Object.class) {
								superMeta = getMeta(superClazz);
							}
							meta = new MetaEmbeddableImpl(clazz, type, (MetaDataObjectImpl) superMeta);
						} else if (clazz.isPrimitive() || clazz == String.class || clazz == Boolean.class
								|| TemporalAccessor.class.isAssignableFrom(clazz)
								|| Number.class.isAssignableFrom(clazz)) {
							meta = new MetaPrimitiveType(clazz, type);
						} else {
							throw new IllegalArgumentException("unable to get meta data for " + clazz.getName());
						}
					} else if (type instanceof ParameterizedType) {
						ParameterizedType paramType = (ParameterizedType) type;
						if (paramType.getRawType() instanceof Class
								&& Map.class.isAssignableFrom((Class<?>) paramType.getRawType())) {
							KatharsisAssert.assertEquals(2, paramType.getActualTypeArguments().length);
							MetaType keyType = getMeta(paramType.getActualTypeArguments()[0]).asType();
							MetaType valueType = getMeta(paramType.getActualTypeArguments()[1]).asType();
							meta = new MetaMapTypeImpl(null, (Class<?>) paramType.getRawType(), type, keyType,
									valueType);
						} else if (paramType.getRawType() instanceof Class
								&& Collection.class.isAssignableFrom((Class<?>) paramType.getRawType())) {
							KatharsisAssert.assertEquals(1, paramType.getActualTypeArguments().length);
							MetaType elementType = getMeta(paramType.getActualTypeArguments()[0]).asType();
							meta = new MetaCollectionTypeImpl(null, (Class<?>) paramType.getRawType(), type,
									elementType);
						} else {
							throw new UnsupportedOperationException("unknown type " + type);
						}
					} else {
						throw new UnsupportedOperationException("unknown type " + type);
					}
					cache.put(type, meta);
					((MetaElementImpl) meta).init();
				}
			}
		}
		return meta;
	}
}

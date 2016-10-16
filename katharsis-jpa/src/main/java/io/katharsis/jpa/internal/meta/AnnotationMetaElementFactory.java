package io.katharsis.jpa.internal.meta;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Factory for MetaElement based on observed annotations.
 */
public interface AnnotationMetaElementFactory {

	public Class<? extends Annotation> getAnnotation();

	public MetaElement create(Type type, MetaLookup lookup);

}

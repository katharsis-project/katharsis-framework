package io.katharsis.module;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Used to integrate Katharsis into various dependency management frameworks and other systems.
 */
public interface ServiceDiscovery {

	public <T> List<T> getInstancesByType(Class<T> clazz);

	public <A extends Annotation> List<Object> getInstancesByAnnotation(Class<A> annotation);
}

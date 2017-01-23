package io.katharsis.core.internal.resource;

import io.katharsis.core.internal.utils.ClassUtils;
import io.katharsis.resource.Resource;
import io.katharsis.resource.information.ResourceInstanceBuilder;

/**
 * Default implementation for {@link ResourceInstanceBuilder}} that creates a new instance of the given class
 * using its default constructor.
 */
public class DefaultResourceInstanceBuilder<T> implements ResourceInstanceBuilder<T> {

	private Class<T> resourceClass;

	public DefaultResourceInstanceBuilder(Class<T> resourceClass) {
		this.resourceClass = resourceClass;
	}

	@Override
	public T buildResource(Resource body) {
		return (T) ClassUtils.newInstance(resourceClass);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resourceClass == null) ? 0 : resourceClass.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultResourceInstanceBuilder<?> other = (DefaultResourceInstanceBuilder<?>) obj;
		if (resourceClass == null) {
			if (other.resourceClass != null)
				return false;
		} else if (!resourceClass.equals(other.resourceClass))
			return false;
		return true;
	}
}

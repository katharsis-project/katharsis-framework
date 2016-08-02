package io.katharsis.resource.information;

import io.katharsis.request.dto.DataBody;
import io.katharsis.utils.ClassUtils;

public class DefaultResourceInstanceBuilder<T> implements ResourceInstanceBuilder<T> {

	private Class<T> resourceClass;

	public DefaultResourceInstanceBuilder(Class<T> resourceClass) {
		this.resourceClass = resourceClass;
	}

	@Override
	public T buildResource(DataBody body) {
		return (T) ClassUtils.newInstance(resourceClass);
	}

}

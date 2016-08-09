package io.katharsis.resource.information;

import io.katharsis.request.dto.DataBody;

/**
 * Used to obtain an instance of the given resource.
 */
public interface ResourceInstanceBuilder<T> {

	// TODO proper interface
	public T buildResource(DataBody body);
}

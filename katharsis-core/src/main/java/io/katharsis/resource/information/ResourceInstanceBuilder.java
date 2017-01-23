package io.katharsis.resource.information;

import io.katharsis.core.internal.resource.DefaultResourceInstanceBuilder;
import io.katharsis.resource.Resource;

/**
 * Used to construct an object instance for the requested resource. {@link DefaultResourceInstanceBuilder} just
 * creates new empty object instances using the default constructor. More elaborate instances may do more, 
 * like binding created entity instances to a JPA session.
 */
public interface ResourceInstanceBuilder<T> {

	/**
	 * @param body request body
	 * @return resource object
	 */
	T buildResource(Resource body);
}
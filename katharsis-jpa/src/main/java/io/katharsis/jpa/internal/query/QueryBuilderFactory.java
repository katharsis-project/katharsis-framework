package io.katharsis.jpa.internal.query;

import java.util.List;

public interface QueryBuilderFactory {

	/**
	 * Builds a new query for the given entity class.
	 */
	<T> QueryBuilder<T> newBuilder(Class<T> entityClass);

	/**
	 * Builds a new query for the given attribute. Used to retrieve relations of
	 * an entity.
	 */
	<T> QueryBuilder<T> newBuilder(Class<?> entityClass, String attrName, List<?> entityIds);

}

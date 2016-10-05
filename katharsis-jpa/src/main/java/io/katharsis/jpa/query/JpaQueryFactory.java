package io.katharsis.jpa.query;

import java.util.List;

public interface JpaQueryFactory {

	/**
	 * Builds a new query for the given entity class.
	 */
	<T> JpaQuery<T> query(Class<T> entityClass);

	/**
	 * Builds a new query for the given attribute. Used to retrieve relations of
	 * an entity.
	 */
	<T> JpaQuery<T> query(Class<?> entityClass, String attrName, List<?> entityIds);

	/**
	 * @return ComputedAttributeRegistry holding registered computed attributes
	 */
	ComputedAttributeRegistry getComputedAttributes();

}

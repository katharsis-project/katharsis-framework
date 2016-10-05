package io.katharsis.jpa.query;

import java.util.Set;

/**
 * Holds the computed attributes registered to a JpaQueryFactory.
 */
public interface ComputedAttributeRegistry {

	/**
	 * @param entityType
	 * @return list of computed attribute names for the given entity type
	 */
	Set<String> getForType(Class<?> entityType);

}

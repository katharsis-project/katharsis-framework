package io.katharsis.meta.information;

import io.katharsis.meta.model.MetaElement;
import io.katharsis.utils.Optional;

/**
 * resource, repository or field information backed by meta data.
 */
public interface MetaAwareInformation<T extends MetaElement> {

	/**
	 * @return meta element of this resource field
	 */
	Optional<T> getMetaElement();

	/**
	 * @return meta element this information element was derived from. Like a JPA attribute mapped to a JsonApi field.
	 */
	Optional<T> getProjectedMetaElement();
}

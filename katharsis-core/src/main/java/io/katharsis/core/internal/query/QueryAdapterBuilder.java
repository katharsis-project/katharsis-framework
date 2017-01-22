package io.katharsis.core.internal.query;

import java.util.Map;
import java.util.Set;

import io.katharsis.repository.request.QueryAdapter;
import io.katharsis.resource.information.ResourceInformation;

/**
 * Builds the query adapter for the given parameters, resulting in either a queryParams or querySpec adapter depending on the chosen implementation.
 */
public interface QueryAdapterBuilder {

	public QueryAdapter build(ResourceInformation resourceInformation, Map<String, Set<String>> parameters);

}

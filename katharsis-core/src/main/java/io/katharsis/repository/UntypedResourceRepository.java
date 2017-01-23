package io.katharsis.repository;

import io.katharsis.resource.Resource;

public interface UntypedResourceRepository extends ResourceRepositoryV2<Resource, String> {

	public String getResourceType();

}

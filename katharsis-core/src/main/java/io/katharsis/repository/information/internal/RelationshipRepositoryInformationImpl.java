package io.katharsis.repository.information.internal;

import io.katharsis.repository.information.RelationshipRepositoryInformation;
import io.katharsis.resource.information.ResourceInformation;

class RelationshipRepositoryInformationImpl extends RepositoryInformationImpl implements RelationshipRepositoryInformation {

	private ResourceInformation sourceResourceInformation;

	public RelationshipRepositoryInformationImpl(Object repository, ResourceInformation sourceResourceInformation,
			ResourceInformation targetResourceInformation) {
		super(repository, targetResourceInformation);
		this.sourceResourceInformation = sourceResourceInformation;
	}

	@Override
	public ResourceInformation getSourceResourceInformation() {
		return sourceResourceInformation;
	}

}

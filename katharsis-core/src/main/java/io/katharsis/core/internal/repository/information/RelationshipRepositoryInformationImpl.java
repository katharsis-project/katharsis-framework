package io.katharsis.core.internal.repository.information;

import io.katharsis.repository.information.RelationshipRepositoryInformation;
import io.katharsis.resource.information.ResourceInformation;

class RelationshipRepositoryInformationImpl extends RepositoryInformationImpl implements RelationshipRepositoryInformation {

	private ResourceInformation sourceResourceInformation;

	public RelationshipRepositoryInformationImpl(Class<?> repositoryClass,ResourceInformation sourceResourceInformation,
			ResourceInformation targetResourceInformation) {
		super(repositoryClass, targetResourceInformation);
		this.sourceResourceInformation = sourceResourceInformation;
	}

	@Override
	public ResourceInformation getSourceResourceInformation() {
		return sourceResourceInformation;
	}
}

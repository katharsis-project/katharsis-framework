package io.katharsis.core.internal.repository.information;

import io.katharsis.repository.information.RepositoryInformation;
import io.katharsis.resource.information.ResourceInformation;

abstract class RepositoryInformationImpl implements RepositoryInformation {

	private ResourceInformation resourceInformation;

	private Class<?> repositoryClass;

	public RepositoryInformationImpl(Class<?> repositoryClass, ResourceInformation resourceInformation) {
		super();
		this.repositoryClass = repositoryClass;
		this.resourceInformation = resourceInformation;
	}

	@Override
	public Class<?> getRepositoryClass() {
		return repositoryClass;
	}

	@Override
	public ResourceInformation getResourceInformation() {
		return resourceInformation;
	}
}
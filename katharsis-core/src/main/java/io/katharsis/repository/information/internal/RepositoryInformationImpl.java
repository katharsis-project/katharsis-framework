package io.katharsis.repository.information.internal;

import io.katharsis.repository.information.RepositoryInformation;
import io.katharsis.resource.information.ResourceInformation;

abstract class RepositoryInformationImpl implements RepositoryInformation {

	private Object repository;

	private ResourceInformation resourceInformation;

	public RepositoryInformationImpl(Object repository, ResourceInformation resourceInformation) {
		super();
		this.repository = repository;
		this.resourceInformation = resourceInformation;
	}

	@Override
	public Object getRepository() {
		return repository;
	}

	@Override
	public ResourceInformation getResourceInformation() {
		return resourceInformation;
	}
}
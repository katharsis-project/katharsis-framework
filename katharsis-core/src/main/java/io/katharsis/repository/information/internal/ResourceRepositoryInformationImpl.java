package io.katharsis.repository.information.internal;

import io.katharsis.repository.information.ResourceRepositoryInformation;
import io.katharsis.resource.information.ResourceInformation;

class ResourceRepositoryInformationImpl extends RepositoryInformationImpl implements ResourceRepositoryInformation {

	private String path;

	public ResourceRepositoryInformationImpl(Object repository, String path, ResourceInformation resourceInformation) {
		super(repository, resourceInformation);
		this.path = path;
	}

	@Override
	public String getPath() {
		return path;
	}

}
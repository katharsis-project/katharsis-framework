package io.katharsis.core.internal.repository.information;

import java.util.HashMap;
import java.util.Map;

import io.katharsis.repository.information.RepositoryAction;
import io.katharsis.repository.information.ResourceRepositoryInformation;
import io.katharsis.resource.information.ResourceInformation;

public class ResourceRepositoryInformationImpl extends RepositoryInformationImpl implements ResourceRepositoryInformation {

	private String path;
	private Map<String, RepositoryAction> actions;

	public ResourceRepositoryInformationImpl(Class<?> repositoryClass, String path, ResourceInformation resourceInformation){
		this(repositoryClass, path, resourceInformation, new HashMap<String, RepositoryAction>());
	}
	
	public ResourceRepositoryInformationImpl(Class<?> repositoryClass, String path,
			ResourceInformation resourceInformation, Map<String, RepositoryAction> actions) {
		super(repositoryClass, resourceInformation);
		this.path = path;
		this.actions = actions;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public Map<String, RepositoryAction> getActions() {
		return actions;
	}
}
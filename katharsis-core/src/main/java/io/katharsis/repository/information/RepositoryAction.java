package io.katharsis.repository.information;

public interface RepositoryAction {

	public enum RepositoryActionType {
		REPOSITORY,
		RESOURCE
	}

	public String getName();

	/**
	 * @return whether a repository or resource action
	 */
	public RepositoryActionType getActionType();
}

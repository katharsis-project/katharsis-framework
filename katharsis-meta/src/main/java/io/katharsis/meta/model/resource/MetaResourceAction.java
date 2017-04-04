package io.katharsis.meta.model.resource;

import io.katharsis.meta.model.MetaElement;
import io.katharsis.resource.annotations.JsonApiResource;

@JsonApiResource(type = "meta/resourceAction")
public class MetaResourceAction extends MetaElement {

	public enum MetaRepositoryActionType {
		REPOSITORY,
		RESOURCE
	}

	private MetaRepositoryActionType actionType;

	public MetaRepositoryActionType getActionType() {
		return actionType;
	}

	public void setActionType(MetaRepositoryActionType actionType) {
		this.actionType = actionType;
	}
}

package io.katharsis.meta.model.resource;

import io.katharsis.meta.model.MetaDataObject;
import io.katharsis.meta.model.MetaElement;
import io.katharsis.resource.annotations.JsonApiResource;

@JsonApiResource(type = "meta/resourceRepository")
public class MetaResourceRepository extends MetaElement {

	private MetaResource resourceType;

	private MetaDataObject listMetaType;

	private MetaDataObject listLinksType;

	public void setResourceType(MetaResource resourceType) {
		this.resourceType = resourceType;
	}

	public MetaResource getResourceType() {
		return resourceType;
	}

	public MetaDataObject getListMetaType() {
		return listMetaType;
	}

	public void setListMetaType(MetaDataObject listMetaType) {
		this.listMetaType = listMetaType;
	}

	public MetaDataObject getListLinksType() {
		return listLinksType;
	}

	public void setListLinksType(MetaDataObject listLinksType) {
		this.listLinksType = listLinksType;
	}
}

package io.katharsis.client.internal;

import io.katharsis.client.internal.proxy.ObjectProxy;
import io.katharsis.jackson.serializer.DataLinksContainerSerializer;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.response.DataLinksContainer;
import io.katharsis.utils.PropertyUtils;

public class ClientDataLinksContainerSerializer extends DataLinksContainerSerializer {

	@Override
	protected boolean getIncludeRelationshipData(DataLinksContainer dataLinksContainer, ResourceField field) {
		// we include relationship data if it is not lazy
		boolean shouldForceFieldInclusion = super.getIncludeRelationshipData(dataLinksContainer, field);
		if (shouldForceFieldInclusion) {
			return true;
		}

		// we also include relationship data if it is not null and not a unloaded proxy
		Object relationshipValue = PropertyUtils.getProperty(dataLinksContainer.getData(), field.getUnderlyingName());

		if (relationshipValue instanceof ObjectProxy) {
			return ((ObjectProxy) relationshipValue).isLoaded();
		}
		else {
			return relationshipValue != null;
		}
	}
}

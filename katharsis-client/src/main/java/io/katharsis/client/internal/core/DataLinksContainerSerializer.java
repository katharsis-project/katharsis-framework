package io.katharsis.client.internal.core;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import io.katharsis.queryParams.include.Inclusion;
import io.katharsis.queryParams.params.IncludedRelationsParams;
import io.katharsis.resource.field.ResourceField;

/**
 * Serializes a <i>links</i> field of a resource in data field of JSON API response.
 * Additionally, it solves a problem
 * mentioned in <a href="https://github.com/katharsis-project/katharsis-core/issues/220#issuecomment-188790551">#220</a>
 * where an included relationship should forced inclusion of relationship details.
 *
 * @see DataLinksContainer
 */
public class DataLinksContainerSerializer extends JsonSerializer<DataLinksContainer> {

	@Override
	public void serialize(DataLinksContainer dataLinksContainer, JsonGenerator gen, SerializerProvider serializers)
			throws IOException {
		gen.writeStartObject();

		for (ResourceField field : dataLinksContainer.getRelationshipFields()) {
			boolean includeRelationshipData = getIncludeRelationshipData(dataLinksContainer, field);
			RelationshipContainer relationshipContainer = new RelationshipContainer(dataLinksContainer, field,
					includeRelationshipData);

			gen.writeObjectField(field.getJsonName(), relationshipContainer);
		}

		gen.writeEndObject();
	}

	protected boolean getIncludeRelationshipData(DataLinksContainer dataLinksContainer, ResourceField field) {
		return field.getIncludeByDefault() || !field.isLazy()
				|| isFieldIncluded(dataLinksContainer.getIncludedRelations(), field.getJsonName(), dataLinksContainer);
	}

	protected boolean isFieldIncluded(IncludedRelationsParams includedRelationsParams, String fieldName,
			DataLinksContainer dataLinksContainer) {
		if (includedRelationsParams == null || includedRelationsParams.getParams() == null) {
			return false;
		}
		int index = dataLinksContainer.getContainer().getIncludedIndex();
		for (Inclusion inclusion : includedRelationsParams.getParams()) {
			if (inclusion.getPathList().size() > index && inclusion.getPathList().get(index).equals(fieldName)) {
				return true;
			}
			else if (dataLinksContainer.getContainer().getAdditionalIndexes() != null) {
				for (int otherIndexes : dataLinksContainer.getContainer().getAdditionalIndexes()) {
					if (inclusion.getPathList().size() > otherIndexes
							&& inclusion.getPathList().get(otherIndexes).equals(fieldName)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public Class<DataLinksContainer> handledType() {
		return DataLinksContainer.class;
	}
}

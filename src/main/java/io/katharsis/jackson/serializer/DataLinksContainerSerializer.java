package io.katharsis.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.queryParams.include.Inclusion;
import io.katharsis.queryParams.params.IncludedRelationsParams;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.DataLinksContainer;
import io.katharsis.response.RelationshipContainer;

import java.io.IOException;

/**
 * Serializes a <i>links</i> field of a resource in data field of JSON API response.
 * Additionally, it solves a problem
 * mentioned in <a href="https://github.com/katharsis-project/katharsis-core/issues/220#issuecomment-188790551">#220</a>
 * where an included relationship should forced inclusion of relationship details.
 *
 * @see DataLinksContainer
 */
public class DataLinksContainerSerializer extends JsonSerializer<DataLinksContainer> {

    public DataLinksContainerSerializer(ResourceRegistry resourceRegistry) {
    }

    @Override
    public void serialize(DataLinksContainer dataLinksContainer, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        for (ResourceField field : dataLinksContainer.getRelationshipFields()) {
            boolean forceInclusion = shouldForceFieldInclusion(field, dataLinksContainer.getIncludedRelations());
            RelationshipContainer relationshipContainer =
                new RelationshipContainer(dataLinksContainer, field, forceInclusion);

            gen.writeObjectField(field.getJsonName(), relationshipContainer);
        }

        gen.writeEndObject();
    }

    private boolean shouldForceFieldInclusion(ResourceField field, IncludedRelationsParams includedRelations) {
        if (includedRelations != null) {
            for (Inclusion inclusion : includedRelations.getParams()) {
                if (field.getJsonName().equals(inclusion.getPath())) {
                    return true;
                }
            }
        }

        return false;
    }

    public Class<DataLinksContainer> handledType() {
        return DataLinksContainer.class;
    }
}

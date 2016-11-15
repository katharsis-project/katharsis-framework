package io.katharsis.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.katharsis.queryParams.include.Inclusion;
import io.katharsis.queryParams.params.IncludedRelationsParams;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.ContainerType;
import io.katharsis.response.DataLinksContainer;
import io.katharsis.response.RelationshipContainer;

import java.io.IOException;
import java.util.List;

/**
 * Serializes a <i>links</i> field of a resource in data field of JSON API response.
 * Additionally, it solves a problem
 * mentioned in <a href="https://github.com/katharsis-project/katharsis-core/issues/220#issuecomment-188790551">#220</a>
 * where an included relationship should forced inclusion of relationship details.
 *
 * @see DataLinksContainer
 */
public class DataLinksContainerSerializer extends JsonSerializer<DataLinksContainer> {

    private ResourceRegistry resourceRegistry;

    public DataLinksContainerSerializer(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public void serialize(DataLinksContainer dataLinksContainer, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        for (ResourceField field : dataLinksContainer.getRelationshipFields()) {
            boolean forceInclusion = shouldForceFieldInclusion(dataLinksContainer, field, dataLinksContainer.getIncludedRelations());
            RelationshipContainer relationshipContainer =
                    new RelationshipContainer(dataLinksContainer, field, forceInclusion);

            gen.writeObjectField(field.getJsonName(), relationshipContainer);
        }

        gen.writeEndObject();
    }

    private boolean shouldForceFieldInclusion(DataLinksContainer dataLinksContainer, ResourceField field, IncludedRelationsParams includedRelations) {

        if (includedRelations == null) {
            return false;
        }

        // if this is a top level container then search all includes first index field name match else search
        if (dataLinksContainer.getContainer().getContainerType().equals(ContainerType.TOP)) {
            for (Inclusion inclusion : includedRelations.getParams()) {
                List<String> pathList = inclusion.getPathList();
                int fieldIndex = dataLinksContainer.getContainer().getIncludedIndex();
                if (pathList.size() > fieldIndex
                        && field.getJsonName().equals(pathList.get(fieldIndex))) {
                    return true;
                }
            }
        } else if (dataLinksContainer.getContainer().getPathList() != null) {
            List<String> pathList = dataLinksContainer.getContainer().getPathList();
            int fieldIndex = dataLinksContainer.getContainer().getIncludedIndex() + 1;
            if (pathList.size() > fieldIndex
                    && field.getJsonName().equals(pathList.get(fieldIndex))) {
                return true;
            }
        }

        return false;
    }

    public Class<DataLinksContainer> handledType() {
        return DataLinksContainer.class;
    }
}

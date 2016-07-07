package io.katharsis.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.katharsis.jackson.exception.JsonSerializationException;
import io.katharsis.queryParams.params.IncludedFieldsParams;
import io.katharsis.queryParams.params.IncludedRelationsParams;
import io.katharsis.queryParams.params.TypedParams;
import io.katharsis.request.dto.Attributes;
import io.katharsis.resource.field.ResourceField;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.response.Container;
import io.katharsis.response.DataLinksContainer;
import io.katharsis.utils.BeanUtils;
import io.katharsis.utils.Predicate2;
import io.katharsis.utils.PropertyUtils;
import io.katharsis.utils.java.Optional;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class serializes an single resource which can be included in <i>data</i> field of JSON API response.
 *
 * @see Container
 */
public class ContainerSerializer extends JsonSerializer<Container> {

    private static final String TYPE_FIELD_NAME = "type";
    private static final String ID_FIELD_NAME = "id";
    private static final String ATTRIBUTES_FIELD_NAME = "attributes";
    private static final String RELATIONSHIPS_FIELD_NAME = "relationships";
    private static final String LINKS_FIELD_NAME = "links";
    private static final String META_FIELD_NAME = "meta";
    private static final String SELF_FIELD_NAME = "self";
    private static final String JACKSON_ATTRIBUTE_FILTER_NAME = "katharsisFilter";

    private final ResourceRegistry resourceRegistry;

    public ContainerSerializer(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public void serialize(Container value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null && value.getData() != null) {
            gen.writeStartObject();

            TypedParams<IncludedFieldsParams> includedFields = null;
            IncludedRelationsParams includedRelationsParams = null;
            if (value.getResponse().getQueryParams() != null) {
                includedFields = value.getResponse()
                        .getQueryParams()
                        .getIncludedFields();
                TypedParams<IncludedRelationsParams> includedRelations = value.getResponse()
                        .getQueryParams()
                        .getIncludedRelations();

                Class<?> dataClass = value.getData().getClass();
                String resourceType = resourceRegistry.getResourceType(dataClass);
                if (includedRelations != null && includedRelations.getParams().containsKey(resourceType)) {
                    includedRelationsParams = includedRelations.getParams().get(resourceType);
                }
            }

            writeData(gen, value.getData(), includedFields, includedRelationsParams);
            gen.writeEndObject();
        } else {
            gen.writeObject(null);
        }
    }

    /**
     * Writes a value. Each serialized container must contain type field whose value is string
     * <a href="http://jsonapi.org/format/#document-structure-resource-types"></a>.
     */
    private void writeData(JsonGenerator gen, Object data, TypedParams<IncludedFieldsParams> includedFields,
                           IncludedRelationsParams includedRelations) throws IOException {
        Class<?> dataClass = data.getClass();
        String resourceType = resourceRegistry.getResourceType(dataClass);

        gen.writeStringField(TYPE_FIELD_NAME, resourceType);

        RegistryEntry entry = resourceRegistry.getEntry(dataClass);
        ResourceInformation resourceInformation = entry.getResourceInformation();
        try {
            writeId(gen, data, resourceInformation.getIdField());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new JsonSerializationException(
                "Error writing id field: " + resourceInformation.getIdField().getUnderlyingName());
        }

        Set<String> notAttributesFields = entry.getResourceInformation().getNotAttributeFields();
        writeAttributes(gen, data, includedFields, notAttributesFields);

        Set<ResourceField> relationshipFields = getRelationshipFields(resourceType, resourceInformation, includedFields);
        writeRelationshipFields(gen, data, relationshipFields, includedRelations);
        writeMetaField(gen, data, entry);
        writeLinksField(gen, data, entry);
    }

    private Set<ResourceField> getRelationshipFields(String resourceType, ResourceInformation resourceInformation,
                                                     TypedParams<IncludedFieldsParams> includedFields) {
        Set<ResourceField> relationshipFields = new HashSet<>();
        Optional<Set<String>> fields = includedFields(resourceType, includedFields);
        if (fields.isPresent()) {
            for (ResourceField resourceField : resourceInformation.getRelationshipFields()) {
                if (fields.get().contains(resourceField.getJsonName())) {
                    relationshipFields.add(resourceField);
                }
            }
        } else {
            relationshipFields.addAll(resourceInformation.getRelationshipFields());
        }

        return relationshipFields;
    }

    /**
     * The id MUST be written as a string
     * <a href="http://jsonapi.org/format/#document-structure-resource-ids">Resource IDs</a>.
     */
    private static void writeId(JsonGenerator gen, Object data, ResourceField idField)
        throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
        String sourceId = BeanUtils.getProperty(data, idField.getUnderlyingName());
        gen.writeObjectField(ID_FIELD_NAME, sourceId);
    }

    /**
     * Writes resource attributes object taking into account <i>fields</i> query params. It doesn't allow writing
     * <i>null</i> resource attributes.
     *
     * @param gen                 Jackson generator
     * @param data                resource object
     * @param includedFields      <i>field</i> query param values
     * @param notAttributesFields names of relationships and id field
     * @throws IOException               if couldn't write attributes
     */
    private void writeAttributes(JsonGenerator gen, final Object data, TypedParams<IncludedFieldsParams> includedFields,
                                 final Set<String> notAttributesFields)
        throws IOException {

        String resourceType = resourceRegistry.getResourceType(data.getClass());

        final Optional<Set<String>> fields = includedFields(resourceType, includedFields);

        Map<String, Object> dataMap;
        if (fields.isPresent()) {
            Predicate2<Object, PropertyWriter> includeChecker = new Predicate2<Object, PropertyWriter>() {
                @Override
                public boolean test(Object bean, PropertyWriter writer) {
                    return bean != data || (fields.get().contains(writer.getName()) &&
                        !notAttributesFields.contains(writer.getName()));
                }
            };
            ObjectMapper om = getObjectMapper(gen, data, includeChecker);
            dataMap = om.convertValue(data, new TypeReference<Map<String, Object>>() {
            });
        } else {
            Predicate2<Object, PropertyWriter> includeChecker = new Predicate2<Object, PropertyWriter>() {
                @Override
                public boolean test(Object bean, PropertyWriter writer) {
                    return bean != data || !notAttributesFields.contains(writer.getName());
                }
            };
            ObjectMapper om = getObjectMapper(gen, data, includeChecker);
            dataMap = om.convertValue(data, new TypeReference<Map<String, Object>>() {
            });
        }


        Attributes attributesObject = new Attributes();
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            if (entry.getValue() != null)
                attributesObject.addAttribute(entry.getKey(), entry.getValue());
        }

        gen.writeObjectField(ATTRIBUTES_FIELD_NAME, attributesObject);
    }

    /**
     * When <i>fields</i> filter is passed in the query params, <b>attributes</b> and <b>relationships</b> should be
     * filtered accordingly to the requested fields. If there are included fields defined for other resources but not
     * for the current one, empty set is returned
     *
     * @param resourceType   JSON API name of a resource
     * @param includedFields <i>field</i> query param values
     * @return true if it should be included in the response, false otherwise
     */
    private static Optional<Set<String>> includedFields(String resourceType, TypedParams<IncludedFieldsParams> includedFields) {
        IncludedFieldsParams typeIncludedFields = findIncludedFields(includedFields, resourceType);
        if (fieldsForOtherResourceSpecified(includedFields, typeIncludedFields)) {
            return Optional.of(Collections.<String>emptySet());
        } else if (noResourceIncludedFieldsSpecified(typeIncludedFields)) {
            return Optional.empty();
        } else {
            return Optional.of(typeIncludedFields.getParams());
        }
    }

    /**
     * Checks if fields for other resource has been specified but not for the processed one
     *
     * @param includedFields     fields to be included
     * @param typeIncludedFields resource fields to be included
     * @return true if fields for other resource has been specified but not for the processed one, false otherwise
     */
    private static boolean fieldsForOtherResourceSpecified(TypedParams<IncludedFieldsParams> includedFields,
                                                           IncludedFieldsParams typeIncludedFields) {
        return includedFields != null &&
            !includedFields.getParams().isEmpty() &&
            noResourceIncludedFieldsSpecified(typeIncludedFields);
    }

    /**
     * Checks if a value has included fields for a resource
     *
     * @param typeIncludedFields found fields set to be checked
     * @return true if there are no resource fields for inclusion, false otherwise
     */
    private static boolean noResourceIncludedFieldsSpecified(IncludedFieldsParams typeIncludedFields) {
        return typeIncludedFields == null || typeIncludedFields.getParams().isEmpty();
    }

    /**
     * Returns included elements for a resource
     *
     * @param includedFields included fields from request
     * @param elementName    resource name
     * @return included field params
     */
    private static IncludedFieldsParams findIncludedFields(TypedParams<IncludedFieldsParams> includedFields, String
        elementName) {
        IncludedFieldsParams includedFieldsParams = null;
        if (includedFields != null) {
            for (Map.Entry<String, IncludedFieldsParams> entry : includedFields.getParams()
                .entrySet()) {
                if (elementName.equals(entry.getKey())) {
                    includedFieldsParams = entry.getValue();
                }
            }
        }
        return includedFieldsParams;
    }

    private static void writeRelationshipFields(JsonGenerator gen, Object data, Set<ResourceField> relationshipFields,
                                                IncludedRelationsParams includedRelations)
        throws IOException {
        DataLinksContainer dataLinksContainer = new DataLinksContainer(data, relationshipFields, includedRelations);
        gen.writeObjectField(RELATIONSHIPS_FIELD_NAME, dataLinksContainer);
    }

    private void writeLinksField(JsonGenerator gen, Object data, RegistryEntry entry) throws IOException {
        gen.writeFieldName(LINKS_FIELD_NAME);
        if (entry.getResourceInformation().getLinksFieldName() != null) {
            gen.writeObject(PropertyUtils.getProperty(data, entry.getResourceInformation().getLinksFieldName()));
        } else {
            gen.writeStartObject();
            writeSelfLink(gen, data);
            gen.writeEndObject();
        }
    }

    private void writeSelfLink(JsonGenerator gen, Object data) throws IOException {
        Class<?> sourceClass = data.getClass();
        String resourceUrl = resourceRegistry.getResourceUrl(sourceClass);
        RegistryEntry entry = resourceRegistry.getEntry(sourceClass);
        ResourceField idField = entry.getResourceInformation().getIdField();

        Object sourceId = PropertyUtils.getProperty(data, idField.getUnderlyingName());
        gen.writeStringField(SELF_FIELD_NAME, resourceUrl + "/" + sourceId);
    }

    private void writeMetaField(JsonGenerator gen, Object data, RegistryEntry entry) throws IOException {
        if (entry.getResourceInformation().getMetaFieldName() != null) {
            gen.writeFieldName(META_FIELD_NAME);
            gen.writeObject(PropertyUtils.getProperty(data, entry.getResourceInformation().getMetaFieldName()));
        }
    }

    public Class<Container> handledType() {
        return Container.class;
    }

    /**
     * Generate a new object mapper and configure the filter to exclude some properties.
     */
    private static ObjectMapper getObjectMapper(JsonGenerator gen, final Object data,
                                                Predicate2<Object, PropertyWriter> includedFields) {
        ObjectMapper attributesObjectMapper = ((ObjectMapper) gen.getCodec())
            .copy();

        FilterProvider fp = new SimpleFilterProvider()
            .addFilter(JACKSON_ATTRIBUTE_FILTER_NAME, new KatharsisFieldPropertyFilter(includedFields));
        attributesObjectMapper.setFilters(fp);

        attributesObjectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            @Override
            public Object findFilterId(Annotated a) {
                Object filterId = null;

                if (a instanceof AnnotatedClass) {
                    AnnotatedClass ac = (AnnotatedClass) a;
                    if (ac.getRawType().equals(data.getClass())) {
                        filterId = JACKSON_ATTRIBUTE_FILTER_NAME;
                    }
                }
                return filterId;
            }
        });

        return attributesObjectMapper;
    }
}

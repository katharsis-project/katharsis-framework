package io.katharsis.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiLinksInformation;
import io.katharsis.resource.annotations.JsonApiMetaInformation;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToOne;
import io.katharsis.resource.exception.init.MultipleJsonApiLinksInformationException;
import io.katharsis.resource.exception.init.MultipleJsonApiMetaInformationException;
import io.katharsis.resource.exception.init.ResourceDuplicateIdException;
import io.katharsis.resource.exception.init.ResourceIdNotFoundException;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.UnAnnotatedTask;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceInformationBuilderTest {

    private static final String NAME_PROPERTY = "underlyingName";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private final ResourceInformationBuilder resourceInformationBuilder = new ResourceInformationBuilder(
        new ResourceFieldNameTransformer());

    @Test
    public void shouldHaveResourceClassInfoForValidResource() throws Exception {
        ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);

        assertThat(resourceInformation.getResourceClass())
            .isNotNull()
            .isEqualTo(Task.class);
    }

    @Test
    public void shouldHaveIdFieldInfoForValidResource() throws Exception {
        ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);

        assertThat(resourceInformation.getIdField().getUnderlyingName())
            .isNotNull()
            .isEqualTo("id");
    }

    @Test
    public void shouldThrowExceptionWhenResourceWithNoIdAnnotation() {
        expectedException.expect(ResourceIdNotFoundException.class);

        resourceInformationBuilder.build(UnAnnotatedTask.class);
    }

    @Test
    public void shouldThrowExceptionWhenMoreThan1IdAnnotationFound() throws Exception {
        expectedException.expect(ResourceDuplicateIdException.class);
        expectedException.expectMessage("Duplicated Id field found in class");

        resourceInformationBuilder.build(DuplicatedIdResource.class);
    }

    @Test
    public void shouldHaveProperRelationshipFieldInfoForValidResource() throws Exception {
        ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);

        assertThat(resourceInformation.getRelationshipFields())
            .isNotNull()
            .hasSize(4)
            .extracting(NAME_PROPERTY)
            .contains("project", "projects");
    }

    @Test
    public void shouldThrowExceptionWhenResourceWithIgnoredIdAnnotation() {
        expectedException.expect(ResourceIdNotFoundException.class);

        resourceInformationBuilder.build(IgnoredIdResource.class);
    }

    @Test
    public void shouldReturnIdFieldBasedOnFieldGetter() throws Exception {
        ResourceInformation resourceInformation = resourceInformationBuilder.build(IdFieldWithAccessorGetterResource.class);

        assertThat(resourceInformation.getIdField())
            .isNotNull();
    }

    @Test
    public void shouldReturnMergedAnnotationsOnAnnotationsOnFieldAndMethod() throws Exception {
        ResourceInformation resourceInformation = resourceInformationBuilder.build(AnnotationOnFieldAndMethodResource.class);

        assertThat(resourceInformation.getRelationshipFields())
            .isNotNull()
            .hasSize(0);
    }

    @Test
    public void shouldContainMetaInformationField() throws Exception {
        ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);

        assertThat(resourceInformation.getMetaFieldName())
            .isEqualTo("metaInformation");
    }

    @Test
    public void shouldThrowExceptionOnMultipleMetaInformationFields() throws Exception {
        ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);

        assertThat(resourceInformation.getMetaFieldName())
            .isEqualTo("metaInformation");
    }

    @Test
    public void shouldContainLinksInformationField() throws Exception {
        expectedException.expect(MultipleJsonApiMetaInformationException.class);

        resourceInformationBuilder.build(MultipleMetaInformationResource.class);
    }

    @Test
    public void shouldThrowExceptionOnMultipleLinksInformationFields() throws Exception {
        expectedException.expect(MultipleJsonApiLinksInformationException.class);

        resourceInformationBuilder.build(MultipleLinksInformationResource.class);
    }

    @JsonApiResource(type = "duplicatedIdAnnotationResources")
    private static class DuplicatedIdResource {
        @JsonApiId
        private Long id;

        @JsonApiId
        private Long id2;
    }

    @JsonApiResource(type = "ignoredId")
    private static class IgnoredIdResource {
        @JsonApiId
        @JsonIgnore
        private Long id;
    }

    @JsonApiResource(type = "ignoredAttribute")
    private static class IgnoredAttributeResource {
        @JsonApiId
        private Long id;

        @JsonIgnore
        private String attribute;
    }

    @JsonApiResource(type = "accessorGetter")
    private static class AccessorGetterResource {
        @JsonApiId
        private Long id;

        private String getAccessorField() {
            return null;
        }
    }

    @JsonApiResource(type = "ignoredAccessorGetter")
    private static class IgnoredAccessorGetterResource {
        @JsonApiId
        private Long id;

        @JsonIgnore
        private String getAccessorField() {
            return null;
        }
    }

    @JsonApiResource(type = "fieldWithAccessorGetterResource")
    private static class FieldWithAccessorGetterResource {
        @JsonApiId
        private Long id;

        public String getAccessorField() {
            return accessorField;
        }

        private String accessorField;
    }

    @JsonApiResource(type = "idFieldWithAccessorGetterResource")
    private static class IdFieldWithAccessorGetterResource {

        @JsonApiId
        public Long getId() {
            return null;
        }
    }

    @JsonApiResource(type = "annotationOnFieldAndMethod")
    private static class AnnotationOnFieldAndMethodResource {
        @JsonApiId
        private Long id;

        @JsonIgnore
        private String field;

        @JsonApiToOne
        private String getField() {
            return null;
        }
    }

    @JsonApiResource(type = "ignoredAttribute")
    private static class IgnoredStaticAttributeResource {
        @JsonApiId
        private Long id;

        public static String attribute;
    }

    @JsonApiResource(type = "ignoredAttribute")
    private static class IgnoredTransientAttributeResource {

        @JsonApiId
        private Long id;

        public transient int attribute;

        public int getAttribute() {
            return attribute;
        }

    }

    @JsonApiResource(type = "ignoredAttribute")
    private static class IgnoredStaticGetterResource {
        @JsonApiId
        private Long id;

        public static int getAttribute() {
            return 0;
        }
    }

    @JsonPropertyOrder({"b", "a", "c"})
    @JsonApiResource(type = "orderedResource")
    private static class OrderedResource {
        @JsonApiId
        private Long id;

        public String c;
        public String b;
        public String a;
    }

    @JsonPropertyOrder(alphabetic = true)
    @JsonApiResource(type = "AlphabeticResource")
    private static class AlphabeticResource {
        @JsonApiId
        private Long id;

        public String c;
        public String b;
        public String a;
    }

    @JsonApiResource(type = "multipleMetaInformationResource")
    private static class MultipleMetaInformationResource {
        @JsonApiId
        private Long id;

        @JsonApiMetaInformation
        public String c;

        @JsonApiMetaInformation
        public String b;
    }

    @JsonApiResource(type = "multipleLinksInformationResource")
    private static class MultipleLinksInformationResource {
        @JsonApiId
        private Long id;

        @JsonApiLinksInformation
        public String c;

        @JsonApiLinksInformation
        public String b;
    }
}

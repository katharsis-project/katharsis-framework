package io.katharsis.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.annotations.JsonApiToOne;
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

    private static final String NAME_PROPERTY = "name";

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

        assertThat(resourceInformation.getIdField().getName())
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
    public void shouldHaveProperBasicFieldInfoForValidResource() throws Exception {
        ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);

        assertThat(resourceInformation.getAttributeFields())
            .isNotNull()
            .hasSize(1)
            .extracting(NAME_PROPERTY)
            .containsOnly("name");
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
    public void shouldHaveNoAttributesInfoForIgnoredField() throws Exception {
        ResourceInformation resourceInformation = resourceInformationBuilder.build(AccessorGetterResource.class);

        assertThat(resourceInformation.getAttributeFields())
            .isNotNull()
            .hasSize(1)
            .extracting(NAME_PROPERTY)
            .containsOnly("accessorField");
    }

    @Test
    public void shouldNotReturnFieldBasedOnAccessorGetterWhenGetterIsIgnored() throws Exception {
        ResourceInformation resourceInformation = resourceInformationBuilder.build(IgnoredAccessorGetterResource.class);

        assertThat(resourceInformation.getAttributeFields())
            .isNotNull()
            .isEmpty();
    }

    @Test
    public void shouldReturnFieldBasedOnFieldOnlyAndIgnoreGetter() throws Exception {
        ResourceInformation resourceInformation = resourceInformationBuilder.build(FieldWithAccessorGetterResource.class);

        assertThat(resourceInformation.getAttributeFields())
            .isNotNull()
            .hasSize(1)
            .extracting(NAME_PROPERTY)
            .containsOnly("accessorField");
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
}

package io.katharsis.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.exception.init.ResourceDuplicateIdException;
import io.katharsis.resource.exception.init.ResourceIdNotFoundException;
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

    private ResourceInformationBuilder resourceInformationBuilder = new ResourceInformationBuilder();

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
                .hasSize(1)
                .extracting(NAME_PROPERTY)
                .containsOnly("project");
    }

    @Test
    public void shouldThrowExceptionWhenResourceWithIgnoredIdAnnotation() {
        expectedException.expect(ResourceIdNotFoundException.class);

        resourceInformationBuilder.build(IgnoredIdResource.class);
    }

    @Test
    public void shouldThrowExceptionWhenResourceWithTransientIdAnnotation() {
        expectedException.expect(ResourceIdNotFoundException.class);

        resourceInformationBuilder.build(TransientIdResource.class);
    }

    @Test
    public void shouldHaveNoAttributesInfoForIgnoredField() throws Exception {
        ResourceInformation resourceInformation = resourceInformationBuilder.build(IgnoredAttributeResource.class);

        assertThat(resourceInformation.getAttributeFields())
                .isNotNull()
                .isEmpty();
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

    @JsonApiResource(type = "transientId")
    private static class TransientIdResource {
        @JsonApiId
        private transient Long id;
    }

    @JsonApiResource(type = "ignoredAttribute")
    private static class IgnoredAttributeResource {
        @JsonApiId
        private Long id;

        @JsonIgnore
        private String attribute;
    }
}

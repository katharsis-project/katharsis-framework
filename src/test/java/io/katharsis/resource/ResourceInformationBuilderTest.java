package io.katharsis.resource;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.exception.ResourceException;
import io.katharsis.resource.exception.ResourceFieldException;
import io.katharsis.resource.exception.ResourceIdNotFoundException;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.UnAnnotatedTask;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class ResourceInformationBuilderTest {

    private static final String NAME_PROPERTY = "name";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ResourceInformationBuilder resourceInformationBuilder = new ResourceInformationBuilder();

    @Test
    public void shouldHaveResourceClassInfoForValidResource() throws Exception {
        ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);

        // WHEN
        ResourceInformation resourceInformation = sut.build(Task.class);

        // THEN
        assertThat(resourceInformation.getResourceClass()).isEqualTo(Task.class);
        Assert.assertEquals("id", resourceInformation.getIdField().getName());
        Assert.assertEquals(1, resourceInformation.getAttributeFields().size());
        Assert.assertEquals("name", resourceInformation.getAttributeFields().iterator().next().getName());
        Assert.assertEquals(1, resourceInformation.getRelationshipFields().size());
        Assert.assertEquals("project", resourceInformation.getRelationshipFields().iterator().next().getName());
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

        // WHEN
        sut.build(UnAnnotatedTask.class);
        resourceInformationBuilder.build(UnAnnotatedTask.class);
    }

    @Test
    public void shouldThrowExceptionWhenMoreThan1IdAnnotationFound() throws Exception {
        expectedException.expect(ResourceException.class);
        expectedException.expectMessage("Duplicated Id field found in class");

        resourceInformationBuilder.build(DuplicatedIdResource.class);
    }

    @Test
    public void shouldHaveProperBasicFieldInfoForValidResource() throws Exception {
        ResourceInformation resourceInformation = resourceInformationBuilder.build(Task.class);

        assertThat(resourceInformation.getBasicFields())
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


    @JsonApiResource(type = "duplicatedIdAnnotationResources")
    private static class DuplicatedIdResource {
        @JsonApiId
        private Long id;

        @JsonApiId
        private Long id2;
    }
}

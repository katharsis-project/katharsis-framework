package io.katharsis.resource;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.exception.ResourceFieldException;
import io.katharsis.resource.exception.ResourceIdNotFoundException;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.UnAnnotatedTask;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

public class ResourceInformationBuilderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void onValidResourceShouldReturnEntityInformation() {
        // GIVEN
        ResourceInformationBuilder sut = new ResourceInformationBuilder();

        // WHEN
        ResourceInformation resourceInformation = sut.build(Task.class);

        // THEN
        Assert.assertEquals("id", resourceInformation.getIdField().getName());
        Assert.assertEquals(1, resourceInformation.getBasicFields().size());
        Assert.assertEquals("name", resourceInformation.getBasicFields().iterator().next().getName());
        Assert.assertEquals(1, resourceInformation.getRelationshipFields().size());
        Assert.assertEquals("project", resourceInformation.getRelationshipFields().iterator().next().getName());
    }

    @Test
    public void onResourceWithNoIdAnnotationShouldThrowException() {
        // GIVEN
        ResourceInformationBuilder sut = new ResourceInformationBuilder();

        // THEN
        expectedException.expect(ResourceIdNotFoundException.class);

        // WHEN
        sut.build(UnAnnotatedTask.class);
    }

    @Test
    public void onResourceWithRestrictedBasicFieldShouldThrowException() {
        // GIVEN
        ResourceInformationBuilder sut = new ResourceInformationBuilder();

        // THEN
        expectedException.expect(ResourceFieldException.class);

        // WHEN
        sut.build(RestrictedBasicFieldResource.class);
    }

    @Test
    public void onResourceWithRestrictedRelationshipFieldShouldThrowException() {
        // GIVEN
        ResourceInformationBuilder sut = new ResourceInformationBuilder();

        // THEN
        expectedException.expect(ResourceFieldException.class);

        // WHEN
        sut.build(RestrictedRelationshipFieldResource.class);
    }

    @JsonApiResource(type = "restrictedBasicFieldResources")
    private static class RestrictedBasicFieldResource {
        @JsonApiId
        private Long id;
        private String meta;
    }

    @JsonApiResource(type = "restrictedRelationshipFieldResources")
    private static class RestrictedRelationshipFieldResource {
        @JsonApiId
        private Long id;
        private List<String> links;
    }
}

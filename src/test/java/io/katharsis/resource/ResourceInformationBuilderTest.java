package io.katharsis.resource;

import io.katharsis.resource.exception.ResourceIdNotFoundException;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.UnAnnotatedTask;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(resourceInformation.getResourceClass()).isEqualTo(Task.class);
        Assert.assertEquals("id", resourceInformation.getIdField().getName());
        Assert.assertEquals(1, resourceInformation.getAttributeFields().size());
        Assert.assertEquals("name", resourceInformation.getAttributeFields().iterator().next().getName());
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
}

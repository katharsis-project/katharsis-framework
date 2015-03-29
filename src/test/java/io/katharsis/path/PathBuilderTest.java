package io.katharsis.path;

import io.katharsis.context.SampleJsonApplicationContext;
import io.katharsis.resource.ResourceInformationBuilder;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.resource.registry.ResourceRegistryBuilderTest;
import io.katharsis.resource.registry.ResourceRegistryTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

public class PathBuilderTest {

    private ResourceRegistry resourceRegistry;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void prepare() {
        ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(new SampleJsonApplicationContext(), new ResourceInformationBuilder());
        resourceRegistry = registryBuilder.build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, ResourceRegistryTest.TEST_MODELS_URL);
    }

    @Test
    public void onEmptyPathShouldThrowException() {
        // GIVEN
        String path = "/";
        PathBuilder sut = new PathBuilder(resourceRegistry);

        // THEN
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Path is empty");

        // WHEN
        sut.buildPath(path);
    }

    @Test
    public void onFlatResourcePathShouldReturnFlatPath() {
        // GIVEN
        String path = "/tasks/";
        PathBuilder sut = new PathBuilder(resourceRegistry);

        // WHEN
        JsonPath jsonPath = sut.buildPath(path);

        // THEN
        JsonPath expectedPath = new ResourcePath("tasks");
        Assert.assertEquals(expectedPath, jsonPath);
        Assert.assertTrue(expectedPath.isCollection());
    }

    @Test
    public void onFlatResourceInstancePathShouldReturnFlatPath() {
        // GIVEN
        String path = "/tasks/1";
        PathBuilder sut = new PathBuilder(resourceRegistry);

        // WHEN
        JsonPath jsonPath = sut.buildPath(path);

        // THEN
        JsonPath expectedPath = new ResourcePath("tasks", new PathIds("1"));
        Assert.assertEquals(expectedPath, jsonPath);
        Assert.assertFalse(expectedPath.isCollection());
    }

    @Test
    public void onNestedResourcePathShouldReturnNestedPath() {
        // GIVEN
        String path = "/tasks/1/project";
        PathBuilder sut = new PathBuilder(resourceRegistry);

        // WHEN
        JsonPath jsonPath = sut.buildPath(path);

        // THEN
        JsonPath expectedPath = new FieldPath("project");
        expectedPath.setParentResource(new ResourcePath("tasks", new PathIds("1")));
        Assert.assertEquals(expectedPath, jsonPath);
    }

    @Test
    public void onNestedResourceInstancePathShouldReturnNestedPath() {
        // GIVEN
        String path = "/tasks/1/project/2";
        PathBuilder sut = new PathBuilder(resourceRegistry);

        // WHEN
        JsonPath jsonPath = sut.buildPath(path);

        // THEN
        JsonPath expectedPath = new FieldPath("project", new PathIds("2"));
        expectedPath.setParentResource(new ResourcePath("tasks", new PathIds("1")));
        Assert.assertEquals(expectedPath, jsonPath);
    }

    @Test
    public void onNestedResourceRelationshipPathShouldReturnNestedPath() {
        // GIVEN
        String path = "/tasks/1/links/project/";
        PathBuilder sut = new PathBuilder(resourceRegistry);

        // WHEN
        JsonPath jsonPath = sut.buildPath(path);

        // THEN
        JsonPath expectedPath = new LinksPath("project");
        expectedPath.setParentResource(new ResourcePath("tasks", new PathIds("1")));
        Assert.assertEquals(expectedPath, jsonPath);
    }

    @Test
    public void onNestedWrongResourceRelationshipPathShouldThrowException() {
        // GIVEN
        String path = "/tasks/1/links/";
        PathBuilder sut = new PathBuilder(resourceRegistry);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("No type field defined after links marker");

        // WHEN
        sut.buildPath(path);

        // THEN - EXCEPTION
    }

    @Test
    public void onResourceFieldShouldReturnFieldPath() {
        // GIVEN
        String path = "/tasks/1/nonExistingField/";
        PathBuilder sut = new PathBuilder(resourceRegistry);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("No type field defined after links marker");

        // WHEN
        sut.buildPath(path);

        // THEN - EXCEPTION
    }

    @Test
    public void onNonExistingResourceShouldThrowException() {
        // GIVEN
        String path = "/nonExistingResource";
        PathBuilder sut = new PathBuilder(resourceRegistry);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Invalid path: /nonExistingResource");

        // WHEN
        sut.buildPath(path);

        // THEN - EXCEPTION
    }

    @Test
    public void onResourceStaringWithLinksShouldThrowException() {
        // GIVEN
        String path = "/links";
        PathBuilder sut = new PathBuilder(resourceRegistry);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Invalid path: /links");

        // WHEN
        sut.buildPath(path);

        // THEN - EXCEPTION
    }

    @Test
    public void onMultipleResourceInstancesPathShouldReturnCollectionPath() {
        // GIVEN
        String path = "/tasks/1,2";
        PathBuilder sut = new PathBuilder(resourceRegistry);

        // WHEN
        JsonPath jsonPath = sut.buildPath(path);

        // THEN
        Assert.assertTrue(jsonPath.isCollection());
        Assert.assertEquals(jsonPath.getIds().getIds(), Arrays.asList("1", "2"));
    }
}

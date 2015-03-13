package io.katharsis.path;

import org.junit.Assert;
import org.junit.Test;

public class PathBuilderTest {

    @Test
    public void onFlatResourcePathShouldReturnFlatPath() throws Exception {
        // GIVEN
        String path = "/resource1/";
        PathBuilder sut = new PathBuilder();

        // WHEN
        ResourcePath resourcePath = sut.buildPath(path);

        // THEN
        ResourcePath expectedPath = new ResourcePath("resource1");
        Assert.assertEquals(expectedPath, resourcePath);
    }

    @Test
    public void onFlatResourceInstancePathShouldReturnFlatPath() throws Exception {
        // GIVEN
        String path = "/resource1/1";
        PathBuilder sut = new PathBuilder();

        // WHEN
        ResourcePath resourcePath = sut.buildPath(path);

        // THEN
        ResourcePath expectedPath = new ResourcePath("resource1", false, new PathIds("1"));
        Assert.assertEquals(expectedPath, resourcePath);
    }

    @Test
    public void onNestedResourcePathShouldReturnNestedPath() throws Exception {
        // GIVEN
        String path = "/resource1/1/resource2";
        PathBuilder sut = new PathBuilder();

        // WHEN
        ResourcePath resourcePath = sut.buildPath(path);

        // THEN
        ResourcePath expectedPath = new ResourcePath("resource2");
        expectedPath.setParentResource(new ResourcePath("resource1", false, new PathIds("1")));
        Assert.assertEquals(expectedPath, resourcePath);
    }

    @Test
    public void onNestedResourceInstancePathShouldReturnNestedPath() throws Exception {
        // GIVEN
        String path = "/resource1/1/resource2/2";
        PathBuilder sut = new PathBuilder();

        // WHEN
        ResourcePath resourcePath = sut.buildPath(path);

        // THEN
        ResourcePath expectedPath = new ResourcePath("resource2", false, new PathIds("2"));
        expectedPath.setParentResource(new ResourcePath("resource1", false, new PathIds("1")));
        Assert.assertEquals(expectedPath, resourcePath);
    }

    @Test
    public void onNestedResourceRelationshipPathShouldReturnNestedPath() throws Exception {
        // GIVEN
        String path = "/resource1/1/links/resource2/";
        PathBuilder sut = new PathBuilder();

        // WHEN
        ResourcePath resourcePath = sut.buildPath(path);

        // THEN
        ResourcePath expectedPath = new ResourcePath("resource2", false);
        expectedPath.setParentResource(new ResourcePath("resource1", true, new PathIds("1")));
        Assert.assertEquals(expectedPath, resourcePath);
    }
}

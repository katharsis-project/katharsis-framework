package io.katharsis.path;

import org.junit.Assert;
import org.junit.Test;

public class PathBuilderTest {

    @Test
    public void onFlatResourcePathShouldReturnFlatPath() throws Exception {
        // GIVEN
        String path = "/resource1/1";
        PathBuilder sut = new PathBuilder();

        // WHEN
        ResourcePath<?> resourcePath = sut.buildPath(path);

        // THEN
        ResourcePath<String> expectedPath = new ResourcePath<>("resource1", new PathIds<>("1"));
        Assert.assertEquals(expectedPath, resourcePath);
    }
}

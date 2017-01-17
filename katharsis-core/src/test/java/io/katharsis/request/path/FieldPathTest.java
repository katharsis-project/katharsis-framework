package io.katharsis.request.path;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class FieldPathTest {

    @Test
    public void onParentWithNoIdsShouldReturnInformationAboutResource() throws Exception {
        // GIVEN
        JsonPath sut = new FieldPath("field");

        String parentName = "resource";
        JsonPath parent = new ResourcePath(parentName);
        sut.setParentResource(parent);

        // WHEN
        boolean isCollection = sut.isCollection();
        String testParentName = sut.getResourceName();

        // THEN
        assertThat(isCollection).isTrue();
        assertThat(testParentName).isEqualTo(parentName);
    }

    @Test
    public void onParentWithOneIdShouldReturnInformationAboutResource() throws Exception {
        // GIVEN
        JsonPath sut = new FieldPath("field");

        String parentName = "resource";
        JsonPath parent = new ResourcePath(parentName, new PathIds(Collections.singletonList("1")));
        sut.setParentResource(parent);

        // WHEN
        boolean isCollection = sut.isCollection();
        String testParentName = sut.getResourceName();

        // THEN
        assertThat(isCollection).isFalse();
        assertThat(testParentName).isEqualTo(parentName);
    }

    @Test
    public void onParentWithManyIdsShouldReturnInformationAboutResource() throws Exception {
        // GIVEN
        JsonPath sut = new FieldPath("field");

        String parentName = "resource";
        JsonPath parent = new ResourcePath(parentName, new PathIds(Arrays.asList("1", "2")));
        sut.setParentResource(parent);

        // WHEN
        boolean isCollection = sut.isCollection();
        String testParentName = sut.getResourceName();

        // THEN
        assertThat(isCollection).isTrue();
        assertThat(testParentName).isEqualTo(parentName);
    }
}

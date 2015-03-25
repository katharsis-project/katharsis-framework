package io.katharsis.path;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonPathTest {

    @Test
    public void onRelationshipPathShouldMarkAsRelationship() throws Exception {
        // GIVEN
        JsonPath parentJsonPath = new JsonPath("tests");
        parentJsonPath.setHasRelationshipMark(true);
        JsonPath sut = new JsonPath("test");
        sut.setParentResource(parentJsonPath);

        // WHEN
        boolean result = sut.isRelationship();

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    public void onRelationshipPathShouldReturnCorrectResourceName() throws Exception {
        // GIVEN
        JsonPath parentJsonPath = new JsonPath("tests");
        parentJsonPath.setHasRelationshipMark(true);
        JsonPath sut = new JsonPath("test");
        sut.setParentResource(parentJsonPath);

        // WHEN
        String resourceName = sut.getResourceName();

        // THEN
        assertThat(resourceName).isEqualTo("tests");
    }

    @Test
    public void onSingleElementPathShouldNotMarkAsRelationship() throws Exception {
        // GIVEN
        JsonPath sut = new JsonPath("cases");

        // WHEN
        boolean result = sut.isRelationship();

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onNonRelationshipPathShouldNotMarkAsRelationship() throws Exception {
        // GIVEN
        JsonPath parentJsonPath = new JsonPath("tests");
        JsonPath sut = new JsonPath("cases");
        sut.setParentResource(parentJsonPath);

        // WHEN
        boolean result = sut.isRelationship();

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onNonRelationshipPathShouldReturnCorrectResourceName() throws Exception {
        // GIVEN
        JsonPath parentJsonPath = new JsonPath("tests");
        JsonPath sut = new JsonPath("cases");
        sut.setParentResource(parentJsonPath);

        // WHEN
        String resourceName = sut.getResourceName();

        // THEN
        assertThat(resourceName).isEqualTo("cases");
    }
}

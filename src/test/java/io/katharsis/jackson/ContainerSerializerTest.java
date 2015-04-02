package io.katharsis.jackson;

import io.katharsis.resource.mock.models.Project;
import io.katharsis.response.Container;
import org.junit.Test;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

public class ContainerSerializerTest extends BaseSerializerTest {

    @Test
    public void onSimpleObjectShouldIncludeType() throws Exception {
        // GIVEN
        Project project = new Project();

        // WHEN
        String result = sut.writeValueAsString(new Container<>(project));

        // THEN
        assertThatJson(result).node("type").isEqualTo("projects");
    }

    @Test
    public void onSimpleObjectShouldIncludeId() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setId(1L);

        // WHEN
        String result = sut.writeValueAsString(new Container<>(project));

        // THEN
        assertThatJson(result).node("id").isEqualTo("\"1\"");
    }

    @Test
    public void onSimpleObjectShouldIncludeBasicFields() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setName("name");

        // WHEN
        String result = sut.writeValueAsString(new Container<>(project));

        // THEN
        assertThatJson(result).node("name").isEqualTo("name");
    }
}

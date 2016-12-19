package io.katharsis.client.legacy.core;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

import org.junit.Test;

import io.katharsis.client.internal.core.Container;
import io.katharsis.resource.mock.models.Project;

public class DataLinksContainerSerializerTest extends BaseSerializerTest {

    @Test
    public void onSimpleObjectShouldIncludeRelationshipsField() throws Exception {
        // GIVEN
        Project project = new Project();

        // WHEN
        String result = sut.writeValueAsString(new Container(project, testResponse));

        // THEN
        assertThatJson(result).node("links").isPresent();
    }

    @Test
    public void onSimpleResourceRelationshipsToDataShouldHaveSelfLink() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setId(1L);

        // WHEN
        String result = sut.writeValueAsString(new Container(project, testResponse));

        // THEN
        assertThatJson(result).node("links.self").isEqualTo("https://service.local/projects/1");
    }
}

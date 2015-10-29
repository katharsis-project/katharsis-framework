package io.katharsis.jackson;

import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.response.Container;
import org.junit.Test;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

public class LinkageDataContainerSerializerTest extends BaseSerializerTest {

    @Test
    public void onRelationshipShouldIncludeRelationshipLinkage() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setId(2L);
        Task task = new Task();
        task.setId(1L);
        task.setProject(project);

        // WHEN
        String result = sut.writeValueAsString(new Container(task, testResponse));

        // THEN
        assertThatJson(result).node("relationships.project.data.type").isEqualTo("projects");
        assertThatJson(result).node("relationships.project.data.id").isEqualTo("\"2\"");
    }
}

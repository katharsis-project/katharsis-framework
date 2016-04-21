package io.katharsis.jackson;

import io.katharsis.resource.mock.models.FancyProject;
import io.katharsis.resource.mock.models.LazyTask;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.User;
import io.katharsis.response.Container;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

public class RelationshipContainerSerializerTest extends BaseSerializerTest {

    @Test
    public void onRelationshipShouldIncludeRelationship() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setId(1L);
        Task task = new Task();
        task.setProject(project);

        // WHEN
        String result = sut.writeValueAsString(new Container(task, testResponse));

        // THEN
        assertThatJson(result).node("relationships.project").isPresent();
    }

    @Test
    public void onRelationshipShouldIncludeRelationshipSelfLink() throws Exception {
        // GIVEN
        Project project = new Project();
        Task task = new Task();
        task.setId(1L);
        task.setProject(project);

        // WHEN
        String result = sut.writeValueAsString(new Container(task, testResponse));

        // THEN
        assertThatJson(result).node("relationships.project.links.self").isEqualTo("https://service.local/tasks/1/relationships/project");
    }

    @Test
    public void onRelationshipShouldIncludeRelationshipRelatedLink() throws Exception {
        // GIVEN
        Project project = new Project();
        Task task = new Task();
        task.setId(1L);
        task.setProject(project);

        // WHEN
        String result = sut.writeValueAsString(new Container(task, testResponse));

        // THEN
        assertThatJson(result).node("relationships.project.links.related").isEqualTo("https://service.local/tasks/1/project");
    }

    @Test
    public void onToOneRelationshipShouldIncludeToOneRelationshipLinkage() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setId(2L);
        Task task = new Task();
        task.setId(1L);
        task.setProject(project);

        // WHEN
        String result = sut.writeValueAsString(new Container(task, testResponse));

        // THEN
        assertThatJson(result).node("relationships.project.data").isPresent();
    }

    @Test
    public void onToManyLazyRelationshipShouldIncludeToOneRelationshipLinkage() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setId(2L);
        LazyTask task = new LazyTask();
        task.setId(1L);
        task.setProjects(Collections.singletonList(project));

        // WHEN
        String result = sut.writeValueAsString(new Container(task, testResponse));

        // THEN
        assertThatJson(result).node("relationships.projects").isPresent();
        assertThatJson(result).node("relationships.projects.data").isAbsent();
    }

    @Test
    public void onToOneNullRelationshipShouldIncludeNullToOneRelationshipLinkage() throws Exception {
        // GIVEN
        Task task = new Task();
        task.setId(1L);

        // WHEN
        String result = sut.writeValueAsString(new Container(task, testResponse));

        // THEN
        assertThatJson(result).node("relationships.project.data").isEqualTo("null");
    }

    @Test
    public void onToManyRelationshipShouldIncludeToManyRelationshipLinkage() throws Exception {
        // GIVEN
        User user = new User();
        user.setId(1L);
        Project project = new Project();
        project.setId(2L);
        user.setAssignedProjects(Collections.singletonList(project));

        // WHEN
        String result = sut.writeValueAsString(new Container(user, testResponse));

        // THEN
        assertThatJson(result).node("relationships.assignedProjects.data").isArray().ofLength(1);
    }

    @Test
    public void onToManyNullRelationshipShouldIncludeNullToManyRelationshipLinkage() throws Exception {
        // GIVEN
        User user = new User();
        user.setId(1L);

        // WHEN
        String result = sut.writeValueAsString(new Container(user, testResponse));

        // THEN
        assertThatJson(result).node("relationships.assignedProjects.data").isArray().ofLength(0);
    }

    @Test
    public void onInheritedResourceShouldSerializeInheritedType() throws Exception {
        // GIVEN
        Task task = new Task();
        FancyProject fancyProject = new FancyProject();
        task.setProject(fancyProject);
        task.setProjects((List)Collections.singletonList(fancyProject));

        // WHEN
        String result = sut.writeValueAsString(new Container(task, testResponse));

        // THEN
        assertThatJson(result).node("relationships.project.data.type").isStringEqualTo("fancy-projects");
        assertThatJson(result).node("relationships.projects.data[0].type").isStringEqualTo("fancy-projects");
    }
}

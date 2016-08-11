package io.katharsis.jackson;

import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.mock.models.*;
import io.katharsis.response.Container;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;
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
    public void onToOneNullRelationshipShouldIncludeToOneRelationshipLinkage() throws Exception {
        // GIVEN
        LazyTask lazyTask = new LazyTask();
        lazyTask.setId(1L);

        // WHEN
        String result = sut.writeValueAsString(new Container(lazyTask, testResponse));

        // THEN
        assertThatJson(result).node("relationships.project.data").isPresent();
    }

    @Test
    public void onToOneNullRelationshipShouldNotIncludeLazyToOneRelationshipLinkage() throws Exception {
        // GIVEN
        LazyTask lazyTask = new LazyTask();
        lazyTask.setId(1L);

        // WHEN
        String result = sut.writeValueAsString(new Container(lazyTask, testResponse));

        // THEN
        assertThatJson(result).node("relationships.lazyProject.data").isAbsent();
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
        task.setProjects((List) Collections.singletonList(fancyProject));

        // WHEN
        String result = sut.writeValueAsString(new Container(task, testResponse));

        // THEN
        assertThatJson(result).node("relationships.project.data.type").isStringEqualTo("fancy-projects");
        assertThatJson(result).node("relationships.projects.data[0].type").isStringEqualTo("fancy-projects");
    }

    @Test
    public void onNestedInclusionShouldReturnIncludedData() throws Exception {

        // GIVEN
        QueryParams queryParams = getRequestParamsWithInclusion("include[tasks]", "project.task");
        Task task = new Task().setId(1L);
        Project includedProject = new Project().setId(2L);
        Task nestedTask = new Task().setId(3L);
        includedProject.setTask(nestedTask);
        task.setProject(includedProject);
        ResourceResponseContext response = new ResourceResponseContext(new JsonApiResponse().setEntity(task),
                new ResourcePath("tasks"), queryParams);

        // WHEN
        String result = sut.writeValueAsString(response);

        // THEN
        assertThatJson(result).node("included[1].type").isStringEqualTo("projects");
        assertThatJson(result).node("included[1].relationships.task.data.id").isPresent();
        assertThatJson(result).node("included[1].relationships.task.data.id").isStringEqualTo("3");
        assertThatJson(result).node("included[0].type").isStringEqualTo("tasks");
        assertThatJson(result).node("included[0].relationships.projects.data").isAbsent();
    }


    @Test
    public void onInclusionShouldReturnNestedDefaultData() throws Exception {

        // GIVEN
        QueryParams queryParams = getRequestParamsWithInclusion("include[tasks]", "project");
        Task task = new Task().setId(1L);
        Project includedProject = new Project().setId(2L);
        ProjectEager nestedDefaultProject = new ProjectEager();
        nestedDefaultProject.setId(3L);
        ProjectEager nestedDefaultProject2 = new ProjectEager();
        nestedDefaultProject2.setId(4L);
        nestedDefaultProject.setName("default");
        includedProject.setProjectEager(nestedDefaultProject);
        includedProject.getProjectEagerList().add(nestedDefaultProject2);
        task.setProject(includedProject);
        ResourceResponseContext response = new ResourceResponseContext(new JsonApiResponse().setEntity(task),
                new ResourcePath("tasks"), queryParams);

        // WHEN
        String result = sut.writeValueAsString(response);

        // THEN
        assertThatJson(result).node("data.type").isStringEqualTo("tasks");
        assertThatJson(result).node("data.relationships.project.data.id").isPresent();
        assertThatJson(result).node("data.relationships.projects.data").isPresent();
        assertThatJson(result).node("data.relationships.includedProject.data").isAbsent();
        assertThatJson(result).node("data.relationships.project.data.id").isStringEqualTo("2");
        assertThatJson(result).node("included[0].type").isStringEqualTo("eager-projects");
        assertThatJson(result).node("included[0].relationships.tasks.data").isAbsent();
        assertThatJson(result).node("included[0].relationships.task.data").isAbsent();
        assertThatJson(result).node("included[1].type").isStringEqualTo("eager-projects");
        assertThatJson(result).node("included[1].relationships.tasks.data").isAbsent();
        assertThatJson(result).node("included[1].relationships.task.data").isAbsent();
        assertThatJson(result).node("included[2].type").isStringEqualTo("projects");
        assertThatJson(result).node("included[2].relationships.projectEager.data.id").isPresent();
        assertThatJson(result).node("included[2].relationships.projectEager.data.id").isStringEqualTo("3");
        assertThatJson(result).node("included[2].relationships.projectEagerList.data[0].id").isPresent();
        assertThatJson(result).node("included[2].relationships.projectEagerList.data[0].id").isStringEqualTo("4");
    }

    private QueryParams getRequestParamsWithInclusion(String resourceType, String relationshipField) {
        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
        return queryParamsBuilder.buildQueryParams(Collections.singletonMap(resourceType, Collections.singleton(relationshipField)));
    }
}

package io.katharsis.jackson;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.mock.models.FancyProject;
import io.katharsis.resource.mock.models.LazyTask;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.ProjectEager;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.User;
import io.katharsis.response.Container;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertNotNull;
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
                new ResourcePath("tasks"), new QueryParamsAdapter(queryParams));

        // WHEN
        String result = sut.writeValueAsString(response);

        // THEN

        ReadContext resultCtx = JsonPath.parse(result);
        if ("projects".equals(resultCtx.read("included[0].type"))) {
            assertThatJson(result).node("included[0].type").isStringEqualTo("projects");
            assertThatJson(result).node("included[0].relationships.task.data.id").isPresent();
            assertThatJson(result).node("included[0].relationships.task.data.id").isStringEqualTo("3");
        } else {
            assertThatJson(result).node("included[0].type").isStringEqualTo("tasks");
            assertThatJson(result).node("included[0].relationships.projects.data").isAbsent();
        }
        if ("projects".equals(resultCtx.read("included[1].type"))) {
            assertThatJson(result).node("included[1].type").isStringEqualTo("projects");
            assertThatJson(result).node("included[1].relationships.task.data.id").isPresent();
            assertThatJson(result).node("included[1].relationships.task.data.id").isStringEqualTo("3");
        } else {
            assertThatJson(result).node("included[1].type").isStringEqualTo("tasks");
            assertThatJson(result).node("included[1].relationships.projects.data").isAbsent();
        }
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
                new ResourcePath("tasks"), new QueryParamsAdapter(queryParams));

        // WHEN
        String result = sut.writeValueAsString(response);

        // THEN
        assertThatJson(result).node("data.type").isStringEqualTo("tasks");
        assertThatJson(result).node("data.relationships.project.data.id").isPresent();
        assertThatJson(result).node("data.relationships.projects.data").isPresent();
        assertThatJson(result).node("data.relationships.includedProject.data").isPresent();
        assertThatJson(result).node("data.relationships.project.data.id").isStringEqualTo("2");

        ReadContext resultCtx = JsonPath.parse(result);
        assertInclude(result, 0, resultCtx);
        assertInclude(result, 1, resultCtx);
        assertInclude(result, 2, resultCtx);

    }

    @Test
    public void onNestedInclusionWithEmptyInitializedArraysShouldNotThrowError() throws Exception {
        // GIVEN
        QueryParams queryParams = getRequestParamsWithInclusion("include[tasks]", "projectsInit.tasks");
        Task task = new Task().setId(1L);
        ResourceResponseContext response = new ResourceResponseContext(new JsonApiResponse().setEntity(task),
                new ResourcePath("tasks"), new QueryParamsAdapter(queryParams));

        // WHEN
        String result = sut.writeValueAsString(response);

        // THEN
        ReadContext resultCtx = JsonPath.parse(result);
        // only confirm we have result ctx back
        assertNotNull(resultCtx);
    }

    private void assertInclude(String result, int index, ReadContext resultCtx) {
        if ("eager-projects".equals(resultCtx.read("included[" + index + "].type"))) {
            assertEagerProject(result, index);
        } else {
            assertProject(result, index);
        }
    }

    private void assertEagerProject(String result, int index) {
        assertThatJson(result).node("included[" + index + "].type").isStringEqualTo("eager-projects");
        assertThatJson(result).node("included[" + index + "].relationships.tasks.data").isPresent();
        assertThatJson(result).node("included[" + index + "].relationships.task.data").isPresent();
    }

    private void assertProject(String result, int index) {
        assertThatJson(result).node("included[" + index + "].type").isStringEqualTo("projects");
        assertThatJson(result).node("included[" + index + "].relationships.projectEager.data.id").isPresent();
        assertThatJson(result).node("included[" + index + "].relationships.projectEager.data.id").isStringEqualTo("3");
        assertThatJson(result).node("included[" + index + "].relationships.projectEagerList.data[0].id").isPresent();
        assertThatJson(result).node("included[" + index + "].relationships.projectEagerList.data[0].id").isStringEqualTo("4");
    }

    private QueryParams getRequestParamsWithInclusion(String resourceType, String relationshipField) {
        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
        return queryParamsBuilder.buildQueryParams(Collections.singletonMap(resourceType, Collections.singleton(relationshipField)));
    }

    @Test
    public void onDeepNestedInclusionShouldReturnIncludedData() throws Exception {

        // GIVEN
        QueryParams queryParams = getRequestParamsWithInclusion("include[tasks]", "project.task.projectsInit");
        Task task = new Task().setId(1L);
        Project includedProject = new Project().setId(2L);
        Task nestedTask = new Task().setId(3L);
        Project deepNestedProject4L = new Project().setId(4L);
        Project deepNestedProject5L = new Project().setId(5L);
        nestedTask.setProjectsInit(Arrays.asList(deepNestedProject4L, deepNestedProject5L));
        includedProject.setTask(nestedTask);
        task.setProject(includedProject);
        ResourceResponseContext response = new ResourceResponseContext(new JsonApiResponse().setEntity(task),
                new ResourcePath("tasks"), new QueryParamsAdapter(queryParams));

        // WHEN
        String result = sut.writeValueAsString(response);

        // THEN
        assertThatJson(result).node("data.type").isStringEqualTo("tasks");
        assertThatJson(result).node("data.id").isStringEqualTo("1");
        assertThatJson(result).node("data.relationships.project.data.id").isStringEqualTo("2");
        assertThatJson(result).node("included[0].type").isStringEqualTo("projects");
        assertThatJson(result).node("included[0].id").isStringEqualTo("2");
        assertThatJson(result).node("included[0].relationships.task.data.id").isStringEqualTo("3");
        // confirm default variables are referenced in first level includes
        assertThatJson(result).node("included[0].relationships.projectEager.data").isEqualTo(null);
        assertThatJson(result).node("included[1].type").isStringEqualTo("tasks");
        assertThatJson(result).node("included[1].id").isStringEqualTo("3");
        // confirm no default variables are referenced in second level nested includes
        assertThatJson(result).node("included[1].relationships.project.data").isAbsent();
        assertThatJson(result).node("included[1].relationships.projectsInit.data[0].type").isStringEqualTo("projects");
        assertThatJson(result).node("included[1].relationships.projectsInit.data[0].id").isStringEqualTo("4");
        assertThatJson(result).node("included[1].relationships.projectsInit.data[1].type").isStringEqualTo("projects");
        assertThatJson(result).node("included[1].relationships.projectsInit.data[1].id").isStringEqualTo("5");
        assertThatJson(result).node("included[2].type").isStringEqualTo("projects");
        assertThatJson(result).node("included[2].id").isStringEqualTo("4");
        // confirm no default variables are referenced in deep nested includes
        assertThatJson(result).node("included[2].relationships.projectEager.data").isAbsent();
        assertThatJson(result).node("included[3].type").isStringEqualTo("projects");
        assertThatJson(result).node("included[3].id").isStringEqualTo("5");
        // confirm no default variables are referenced in deep nested includes
        assertThatJson(result).node("included[3].relationships.projectEager.data").isAbsent();
    }

    @Test
    public void onMultipleNestedIncludes() throws Exception {
        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
        Map<String, Set<String>> queryParamsMap = new HashMap<>();
        queryParamsMap.put("include[tasks]", new HashSet<>(Arrays.asList("project.task.projectsInit", "projectsInit.tasks")));
        QueryParams queryParams = queryParamsBuilder.buildQueryParams(queryParamsMap);

        Task task = new Task().setId(1L);
        Project projectInit6L = new Project().setId(6L);
        Project projectInit7L = new Project().setId(7L);
        Task task8L = new Task().setId(8L);
        projectInit6L.setTasks(Collections.singletonList(task8L));
        task.setProjectsInit(Arrays.asList(projectInit6L, projectInit7L));
        Project includedProject = new Project().setId(2L);
        Task nestedTask = new Task().setId(3L);
        Project deepNestedProject4L = new Project().setId(4L);
        Project deepNestedProject5L = new Project().setId(5L);
        nestedTask.setProjectsInit(Arrays.asList(deepNestedProject4L, deepNestedProject5L));
        includedProject.setTask(nestedTask);
        task.setProject(includedProject);
        ResourceResponseContext response = new ResourceResponseContext(new JsonApiResponse().setEntity(task),
                new ResourcePath("tasks"), new QueryParamsAdapter(queryParams));

        // WHEN
        String result = sut.writeValueAsString(response);

        // THEN
        assertThatJson(result).node("data.type").isStringEqualTo("tasks");
        assertThatJson(result).node("data.id").isStringEqualTo("1");
        assertThatJson(result).node("data.relationships.project.data.id").isStringEqualTo("2");
        assertThatJson(result).node("included[0].type").isStringEqualTo("projects");
        assertThatJson(result).node("included[0].id").isStringEqualTo("2");
        assertThatJson(result).node("included[0].relationships.task.data.id").isStringEqualTo("3");
        // confirm default variables are referenced in first level includes
        assertThatJson(result).node("included[0].relationships.projectEager.data").isEqualTo(null);
        assertThatJson(result).node("included[1].type").isStringEqualTo("projects");
        assertThatJson(result).node("included[1].id").isStringEqualTo("6");
        // confirm no default variables are referenced in second level nested includes
        assertThatJson(result).node("included[1].relationships.project.data").isAbsent();
        assertThatJson(result).node("included[1].relationships.tasks.data[0].type").isStringEqualTo("tasks");
        assertThatJson(result).node("included[1].relationships.tasks.data[0].id").isStringEqualTo("8");
        assertThatJson(result).node("included[2].type").isStringEqualTo("tasks");
        assertThatJson(result).node("included[2].id").isStringEqualTo("8");
        // confirm no default variables are referenced in deep nested includes
        assertThatJson(result).node("included[2].relationships.project.data").isAbsent();
        assertThatJson(result).node("included[3].type").isStringEqualTo("projects");
        assertThatJson(result).node("included[3].id").isStringEqualTo("7");
        // confirm no default variables are referenced in second level nested includes
        assertThatJson(result).node("included[3].relationships.project.data").isAbsent();
        assertThatJson(result).node("included[3].relationships.tasks.data").isArray();
        assertThatJson(result).node("included[4].type").isStringEqualTo("tasks");
        assertThatJson(result).node("included[4].id").isStringEqualTo("3");
        // confirm no default variables are referenced in second level nested includes
        assertThatJson(result).node("included[4].relationships.project.data").isAbsent();
        assertThatJson(result).node("included[4].relationships.projectsInit.data[0].type").isStringEqualTo("projects");
        assertThatJson(result).node("included[4].relationships.projectsInit.data[0].id").isStringEqualTo("4");
        assertThatJson(result).node("included[4].relationships.projectsInit.data[1].type").isStringEqualTo("projects");
        assertThatJson(result).node("included[4].relationships.projectsInit.data[1].id").isStringEqualTo("5");
        assertThatJson(result).node("included[5].type").isStringEqualTo("projects");
        assertThatJson(result).node("included[5].id").isStringEqualTo("4");
        // confirm no default variables are referenced in deep nested includes
        assertThatJson(result).node("included[5].relationships.projectEager.data").isAbsent();
        assertThatJson(result).node("included[6].type").isStringEqualTo("projects");
        assertThatJson(result).node("included[6].id").isStringEqualTo("5");
        // confirm no default variables are referenced in deep nested includes
        assertThatJson(result).node("included[6].relationships.projectEager.data").isAbsent();
    }
}

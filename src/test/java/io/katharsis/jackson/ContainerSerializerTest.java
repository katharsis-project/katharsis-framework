package io.katharsis.jackson;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.response.Container;
import io.katharsis.response.ResourceResponse;
import org.junit.Test;

import java.util.Collections;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

public class ContainerSerializerTest extends BaseSerializerTest {

    @Test
    public void onSimpleObjectShouldIncludeType() throws Exception {
        // GIVEN
        Project project = new Project();

        // WHEN
        String result = sut.writeValueAsString(new Container(project, testResponse));

        // THEN
        assertThatJson(result).node("type").isEqualTo("projects");
    }

    @Test
    public void onSimpleObjectShouldIncludeStringId() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setId(1L);

        // WHEN
        String result = sut.writeValueAsString(new Container(project, testResponse));

        // THEN
        assertThatJson(result).node("id").isEqualTo("\"1\"");
    }

    @Test
    public void onSimpleObjectShouldIncludeAttributes() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setName("name");

        // WHEN
        String result = sut.writeValueAsString(new Container(project, testResponse));

        // THEN
        assertThatJson(result).node("attributes.name").isEqualTo("name");
    }

    @Test
    public void onIncludedFieldsInParamsShouldContainIncludedList() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setName("name");
        project.setDescription("description");

        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder();
        QueryParams queryParams = queryParamsBuilder.buildQueryParams(
            Collections.singletonMap("fields[projects]", Collections.singleton("name")));
        JsonPath jsonPath = new PathBuilder(resourceRegistry).buildPath("/projects");

        // WHEN
        String result = sut.writeValueAsString(new Container(project, new ResourceResponse(null, jsonPath, queryParams,
            null, null)));

        // THEN
        assertThatJson(result).node("attributes.name").isEqualTo("name");
        assertThatJson(result).node("attributes.description").isAbsent();
    }

    @Test
    public void onIncludedRelationshipInParamsShouldContainIncludedList() throws Exception {
        // GIVEN
        Task task = new Task();
        task.setName("some name");
        Project project = new Project();
        project.setId(1L);
        task.setProject(project);

        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder();
        QueryParams queryParams = queryParamsBuilder.buildQueryParams(
            Collections.singletonMap("fields[tasks]", Collections.singleton("project")));
        JsonPath jsonPath = new PathBuilder(resourceRegistry).buildPath("/tasks");

        // WHEN
        String result = sut.writeValueAsString(new Container(task, new ResourceResponse(null, jsonPath, queryParams,
            null, null)));

        // THEN
        assertThatJson(result).node("relationships.project").isPresent();
        assertThatJson(result).node("attributes.name").isAbsent();
    }

    @Test
    public void onIncludedAttributesInOtherResourceShouldNotContainFields() throws Exception {
        // GIVEN
        Task task = new Task();
        task.setName("some name");
        Project project = new Project();
        project.setId(1L);
        task.setProject(project);

        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder();
        QueryParams queryParams = queryParamsBuilder.buildQueryParams(
            Collections.singletonMap("fields[projects]", Collections.singleton("name")));
        JsonPath jsonPath = new PathBuilder(resourceRegistry).buildPath("/tasks");

        // WHEN
        String result = sut.writeValueAsString(new Container(task, new ResourceResponse(null, jsonPath, queryParams,
            null, null)));

        // THEN
        assertThatJson(result).node("relationships.project").isAbsent();
        assertThatJson(result).node("attributes.name").isAbsent();
    }
}

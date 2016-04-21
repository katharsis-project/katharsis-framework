package io.katharsis.jackson;

import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.resource.mock.models.LazyTask;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.response.Container;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.ResourceResponseContext;
import org.junit.Test;

import java.util.Collections;

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

    @Test
    public void onLazyRelationshipWithInclusionShouldIncludeDataField() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setId(2L);
        LazyTask task = new LazyTask();
        task.setId(1L);
        task.setProjects(Collections.singletonList(project));

        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
        QueryParams queryParams = queryParamsBuilder.buildQueryParams(
            Collections.singletonMap("include[lazy_tasks]", Collections.singleton("projects")));
        JsonPath jsonPath = new PathBuilder(resourceRegistry).buildPath("/lazy_tasks");

        // WHEN
        String result = sut.writeValueAsString(new Container(task,
            new ResourceResponseContext(new JsonApiResponse(), jsonPath, queryParams)));

        // THEN
        assertThatJson(result).node("relationships.projects.data[0].type").isEqualTo("projects");
        assertThatJson(result).node("relationships.projects.data[0].id").isEqualTo("\"2\"");
    }
}

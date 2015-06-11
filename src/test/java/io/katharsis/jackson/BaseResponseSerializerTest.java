package io.katharsis.jackson;

import com.fasterxml.jackson.databind.JsonMappingException;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.User;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.CollectionResponse;
import io.katharsis.response.Container;
import io.katharsis.response.ResourceResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

public class BaseResponseSerializerTest extends BaseSerializerTest {

    private static final RequestParams REQUEST_PARAMS = new RequestParams(null);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void onSingleResponseWithOneIncludedResourceShouldReturnIncludedResource() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setId(2L);
        project.setName("Sample project");
        Task task = new Task();
        task.setId(1L);
        task.setName("Sample task");
        task.setProject(project);

        // WHEN

        String result = sut.writeValueAsString(new ResourceResponse(new Container<>(task), new ResourcePath("projects"), REQUEST_PARAMS));

        // THEN
        assertThatJson(result).node("data").isPresent();
        assertThatJson(result).node("data.id").isEqualTo("\"1\"");
        assertThatJson(result).node("included").isArray().ofLength(1);
        assertThatJson(result).node("included[0].id").isEqualTo("\"2\"");
    }

    @Test
    public void onSingleResponseWithOneIncludedResourcesShouldReturnIncludedResources() throws Exception {
        // GIVEN
        Project project1 = new Project();
        project1.setId(1L);
        project1.setName("Sample project");
        Project project2 = new Project();
        project2.setId(2L);
        project2.setName("Sample project");

        User user = new User();
        user.setId(1L);
        user.setAssignedProjects(Arrays.asList(project1, project2));

        // WHEN
        String result = sut.writeValueAsString(new ResourceResponse(new Container<>(user), new ResourcePath("projects"), REQUEST_PARAMS));

        // THEN
        assertThatJson(result).node("data").isPresent();
        assertThatJson(result).node("data.id").isEqualTo("\"1\"");
        assertThatJson(result).node("included").isArray().ofLength(2);
    }

    @Test
    public void onSingleResponseWithManyResourcesShouldReturnArrayOfResources() throws Exception {
        // GIVEN
        Task task1 = new Task();
        task1.setId(1L);
        task1.setName("Sample task");
        Task task2 = new Task();
        task2.setId(2L);
        task2.setName("Sample task");

        // WHEN
        String result = sut.writeValueAsString(new CollectionResponse(Arrays.asList(new Container<>(task1), new
                Container<>(task2)), new ResourcePath("tasks"), REQUEST_PARAMS));

        // THEN
        assertThatJson(result).node("data").isArray().ofLength(2);
    }

    @Test
    public void onSingleResponseWithNoResourcesShouldReturnEmptyArray() throws Exception {
        // WHEN
        String result = sut.writeValueAsString(new CollectionResponse(null, new ResourcePath("projects"), REQUEST_PARAMS));

        // THEN
        assertThatJson(result).node("data").isArray().ofLength(0);
    }

    @Test
    public void onSingleResponseWithNoResourceShouldReturnNull() throws Exception {
        // WHEN
        String result = sut.writeValueAsString(new ResourceResponse(null, new ResourcePath("projects"), REQUEST_PARAMS));

        // THEN
        assertThatJson(result).node("data").isEqualTo(null);
    }

    @Test
    public void onCustomResponseShouldThrowException() throws Exception {
        // THEN
        expectedException.expect(JsonMappingException.class);

        // WHEN
        sut.writeValueAsString(new BaseResponse<Object>() {
            @Override
            public int getHttpStatus() {
                return 0;
            }

            @Override
            public Object getData() {
                return null;
            }

            @Override
            public JsonPath getJsonPath() {
                return null;
            }

            @Override
            public RequestParams getRequestParams() {
                return null;
            }
        });
    }
}

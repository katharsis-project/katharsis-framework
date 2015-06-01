package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.request.dto.Attributes;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.Container;
import io.katharsis.response.ResourceResponse;
import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourcePatchTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = "PATCH";

    @Test
    public void onGivenRequestCollectionGetShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/");
        ResourcePatch sut = new ResourcePatch(resourceRegistry, typeParser);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        Assert.assertEquals(result, false);
    }

    @Test
    public void onGivenRequestResourceGetShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/1");
        ResourcePatch sut = new ResourcePatch(resourceRegistry, typeParser);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        Assert.assertEquals(result, true);
    }

    @Test
    public void onNoBodyResourceShouldThrowException() throws Exception {
        // GIVEN
        ResourcePost sut = new ResourcePost(resourceRegistry, typeParser);

        // THEN
        expectedException.expect(RuntimeException.class);

        // WHEN
        sut.handle(new ResourcePath("fridges"), new RequestParams(new ObjectMapper()), null);
    }

    @Test
    public void onGivenRequestResourceGetShouldHandleIt() throws Exception {
        // GIVEN
        RequestBody newTaskBody = new RequestBody();
        newTaskBody.setData(new DataBody());
        newTaskBody.getData().setType("tasks");
        newTaskBody.getData().setAttributes(new Attributes());
        newTaskBody.getData().getAttributes().addAttribute("name", "sample task");

        JsonPath taskPath = pathBuilder.buildPath("/tasks");

        // WHEN
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser);
        ResourceResponse taskResponse = resourcePost.handle(taskPath, new RequestParams(new ObjectMapper()), newTaskBody);
        assertThat(taskResponse.getData()).isExactlyInstanceOf(Container.class);
        assertThat(((Container) taskResponse.getData()).getData()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (((Container) taskResponse.getData()).getData())).getId();
        assertThat(taskId).isNotNull();

        // GIVEN
        RequestBody taskPatch = new RequestBody();
        taskPatch.setData(new DataBody());
        taskPatch.getData().setType("tasks");
        taskPatch.getData().setAttributes(new Attributes());
        taskPatch.getData().getAttributes().addAttribute("name", "task updated");
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/" + taskId);
        ResourcePatch sut = new ResourcePatch(resourceRegistry, typeParser);

        // WHEN
        BaseResponse<?> response = sut.handle(jsonPath, new RequestParams(new ObjectMapper()), taskPatch);

        // THEN
        Assert.assertNotNull(response);
        assertThat(response.getData()).isExactlyInstanceOf(Container.class);
        assertThat(((Container) response.getData()).getData()).isExactlyInstanceOf(Task.class);
        assertThat(((Task) (((Container) response.getData()).getData())).getName()).isEqualTo("task updated");
    }
}

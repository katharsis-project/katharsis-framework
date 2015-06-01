package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.request.dto.Attributes;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.dto.ResourceRelationships;
import io.katharsis.request.path.JsonPath;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.response.BaseResponse;
import io.katharsis.response.Container;
import io.katharsis.response.ResourceResponse;
import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceGetTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = "GET";

    @Test
    public void onGivenRequestCollectionGetShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/");
        ResourceGet sut = new ResourceGet(resourceRegistry, typeParser);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        Assert.assertEquals(result, false);
    }

    @Test
    public void onGivenRequestResourceGetShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/2");
        ResourceGet sut = new ResourceGet(resourceRegistry, typeParser);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        Assert.assertEquals(result, true);
    }

    @Test
    public void onGivenRequestResourceGetShouldHandleIt() throws Exception {
        // GIVEN
        RequestBody newTaskBody = new RequestBody();
        newTaskBody.setData(new DataBody());
        newTaskBody.getData().setType("tasks");
        newTaskBody.getData().setAttributes(new Attributes());
        newTaskBody.getData().getAttributes().addAttribute("name", "sample task");
        newTaskBody.getData().setRelationships(new ResourceRelationships());

        JsonPath taskPath = pathBuilder.buildPath("/tasks");

        // WHEN
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser);
        ResourceResponse taskResponse = resourcePost.handle(taskPath, new RequestParams(new ObjectMapper()), newTaskBody);
        assertThat(taskResponse.getData()).isExactlyInstanceOf(Container.class);
        assertThat(((Container) taskResponse.getData()).getData()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (((Container) taskResponse.getData()).getData())).getId();
        assertThat(taskId).isNotNull();

        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/" + taskId);
        ResourceGet sut = new ResourceGet(resourceRegistry, typeParser);

        // WHEN
        BaseResponse<?> response = sut.handle(jsonPath, new RequestParams(new ObjectMapper()), null);

        // THEN
        Assert.assertNotNull(response);
    }
}

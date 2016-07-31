package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.request.Request;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.dto.ResourceRelationships;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.resource.exception.RequestBodyException;
import io.katharsis.resource.mock.models.ComplexPojo;
import io.katharsis.resource.mock.models.Memorandum;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.response.BaseResponseContext;
import org.junit.Assert;
import org.junit.Test;

import static io.katharsis.dispatcher.controller.HttpMethod.PATCH;
import static org.assertj.core.api.Assertions.assertThat;

public class ResourcePatchTest extends BaseControllerTest {

    @Test
    public void onGivenRequestCollectionGetShouldDenyIt() {
        // GIVEN
        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks");
        Request request = new Request(jsonPath, PATCH.name(), null, parameterProvider);

        ResourcePatch sut = new ResourcePatch(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        Assert.assertEquals(result, false);
    }

    @Test
    public void onGivenRequestResourceGetShouldAcceptIt() {
        // GIVEN
        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/1");
        Request request = new Request(jsonPath, PATCH.name(), null, parameterProvider);

        ResourcePatch sut = new ResourcePatch(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        Assert.assertEquals(result, true);
    }

    @Test
    public void onNoBodyResourceShouldThrowException() throws Exception {
        // GIVEN
        ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // THEN
        expectedException.expect(RuntimeException.class);
        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/fridges");
        Request request = new Request(jsonPath, PATCH.name(), null, parameterProvider);

        // WHEN
        sut.handle(request);
    }

    @Test
    public void onGivenRequestResourceGetShouldHandleIt() throws Exception {
        // GIVEN
        RequestBody newTaskBody = new RequestBody();
        DataBody data = new DataBody();
        newTaskBody.setData(data);
        data.setType("tasks");
        data.setAttributes(objectMapper.createObjectNode()
                .put("name", "sample task"));

        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks");
        Request request = new Request(jsonPath, PATCH.name(), serialize(newTaskBody), parameterProvider);

        // WHEN
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);
        BaseResponseContext taskResponse = resourcePost.handle(request);
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (taskResponse.getResponse().getEntity())).getId();
        assertThat(taskId).isNotNull();

        // GIVEN
        RequestBody taskPatch = new RequestBody();
        data = new DataBody();
        taskPatch.setData(data);
        data.setType("tasks");
        data.setAttributes(objectMapper.createObjectNode()
                .put("name", "task updated"));
        jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/" + taskId);
        request = new Request(jsonPath, PATCH.name(), serialize(taskPatch), parameterProvider);
        ResourcePatch sut = new ResourcePatch(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        BaseResponseContext response = sut.handle(request);

        // THEN
        Assert.assertNotNull(response);
        assertThat(response.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        assertThat(((Task) (response.getResponse().getEntity())).getName()).isEqualTo("task updated");
    }

    @Test
    public void onGivenRequestResourceShouldThrowException() throws Exception {
        // GIVEN
        RequestBody newTaskBody = new RequestBody();
        DataBody data = new DataBody();
        newTaskBody.setData(data);
        data.setType("tasks");
        data.setAttributes(objectMapper.createObjectNode()
                .put("name", "sample task"));

        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks");
        Request request = new Request(jsonPath, PATCH.name(), serialize(newTaskBody), parameterProvider);

        // WHEN
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);
        BaseResponseContext taskResponse = resourcePost.handle(request);
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (taskResponse.getResponse().getEntity())).getId();
        assertThat(taskId).isNotNull();

        // GIVEN

        data = new DataBody();
        data.setType("WRONG_AND_MISSING_TYPE");
        data.setAttributes(objectMapper.createObjectNode().put("name", "task updated"));
        RequestBody taskPatch = new RequestBody(data);

        jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/" + taskId);
        request = new Request(jsonPath, PATCH.name(), serialize(taskPatch), parameterProvider);

        ResourcePatch sut = new ResourcePatch(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN

        try {
            BaseResponseContext response = sut.handle(request);
            Assert.fail("Should have recieved exception.");
        } catch (RequestBodyException rbe) {
            // Got correct exception
        } catch (Error ex) {
            Assert.fail("Got bad exception: " + ex);
        }
    }

    @Test
    public void onInheritedResourceShouldUpdateInheritedResource() throws Exception {
        // GIVEN
        RequestBody memorandumBody = new RequestBody();
        DataBody data = new DataBody();
        memorandumBody.setData(data);
        data.setType("memoranda");
        ObjectNode attributes = objectMapper.createObjectNode()
                .put("title", "sample title")
                .put("body", "sample body");
        data.setAttributes(attributes);

        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/documents");
        Request request = new Request(jsonPath, PATCH.name(), serialize(memorandumBody), parameterProvider);

        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        BaseResponseContext taskResponse = resourcePost.handle(request);

        // THEN
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Memorandum.class);
        Long memorandumId = ((Memorandum) (taskResponse.getResponse().getEntity())).getId();
        assertThat(memorandumId).isNotNull();

        // --------------------------

        // GIVEN
        memorandumBody = new RequestBody();
        data = new DataBody();
        memorandumBody.setData(data);
        data.setType("memoranda");
        data.setAttributes(objectMapper.createObjectNode()
                .put("title", "new title")
                .put("body", "new body"));

        JsonApiPath documentPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/documents/" + memorandumId);
        request = new Request(documentPath, PATCH.name(), serialize(memorandumBody), parameterProvider);

        ResourcePatch sut = new ResourcePatch(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        BaseResponseContext memorandumResponse = sut.handle(request);

        // THEN
        assertThat(memorandumResponse.getResponse().getEntity()).isExactlyInstanceOf(Memorandum.class);
        Memorandum persistedMemorandum = (Memorandum) (memorandumResponse.getResponse().getEntity());
        assertThat(persistedMemorandum.getId()).isNotNull();
        assertThat(persistedMemorandum.getTitle()).isEqualTo("new title");
        assertThat(persistedMemorandum.getBody()).isEqualTo("new body");
    }

    @Test
    public void onResourceRelationshipNullifiedShouldSaveIt() throws Exception {
        // GIVEN
        RequestBody newTaskBody = new RequestBody();
        DataBody data = new DataBody();
        newTaskBody.setData(data);
        data.setType("tasks");
        data.setAttributes(objectMapper.createObjectNode()
                .put("name", "sample task"));

        JsonApiPath documentPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks");
        Request request = new Request(documentPath, PATCH.name(), serialize(newTaskBody), parameterProvider);

        // WHEN
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);
        BaseResponseContext taskResponse = resourcePost.handle(request);
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (taskResponse.getResponse().getEntity())).getId();
        assertThat(taskId).isNotNull();

        // GIVEN
        RequestBody taskPatch = new RequestBody();
        data = new DataBody();
        taskPatch.setData(data);
        data.setType("tasks");
        data.setAttributes(objectMapper.createObjectNode()
                .put("name", "task updated"));
        data.setRelationships(new ResourceRelationships());
        data.getRelationships()
                .setAdditionalProperty("project", null);
        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/" + taskId);
        request = new Request(jsonPath, PATCH.name(), serialize(taskPatch), parameterProvider);

        ResourcePatch sut = new ResourcePatch(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        BaseResponseContext response = sut.handle(request);

        // THEN
        Assert.assertNotNull(response);
        assertThat(response.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        assertThat(((Task) (response.getResponse().getEntity())).getName()).isEqualTo("task updated");
        assertThat(((Task) (response.getResponse().getEntity())).getProject()).isNull();
    }

    @Test
    public void onGivenRequestResourcePatchShouldHandleMissingFields() throws Exception {

        JsonApiPath complexPojoPath = JsonApiPath.parsePathFromStringUrl("http://local/complexpojos/1");

        // WHEN
        ResourceGet resourceGet = new ResourceGet(resourceRegistry, typeParser, includeFieldSetter, queryParamsBuilder, objectMapper);
        Request request = new Request(complexPojoPath, PATCH.name(), null, parameterProvider);
        BaseResponseContext complexPojoResponse = resourceGet.handle(request);

        assertThat(complexPojoResponse.getResponse().getEntity()).isExactlyInstanceOf(ComplexPojo.class);
        Long complexPojoId = ((ComplexPojo) (complexPojoResponse.getResponse().getEntity())).getId();
        assertThat(complexPojoId).isNotNull();
        assertThat(((ComplexPojo) (complexPojoResponse.getResponse().getEntity())).getContainedPojo().getUpdateableProperty1()).isEqualTo("value from repository mock");

        // GIVEN
        RequestBody complexPojoPatch = new RequestBody();
        DataBody data = new DataBody();
        complexPojoPatch.setData(data);
        data.setType("complexpojos");
        JsonNode patchAttributes = objectMapper.readTree("{\"containedPojo\": { \"updateableProperty1\":\"updated value\"}}");
        data.setAttributes(patchAttributes);

        ResourcePatch sut = new ResourcePatch(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        JsonApiPath jsonPath = JsonApiPath.parsePathFromStringUrl("http://local/complexpojos/" + complexPojoId);
        request = new Request(jsonPath, PATCH.name(), serialize(complexPojoPatch), parameterProvider);
        BaseResponseContext response = sut.handle(request);

        // THEN
        Assert.assertNotNull(response);
        assertThat(response.getResponse().getEntity()).isExactlyInstanceOf(ComplexPojo.class);
        assertThat(((ComplexPojo) (response.getResponse().getEntity())).getContainedPojo().getUpdateableProperty1()).isEqualTo("updated value");
        assertThat(((ComplexPojo) (response.getResponse().getEntity())).getContainedPojo().getUpdateableProperty2()).isEqualTo("value from repository mock");
    }

}

package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.dto.ResourceRelationships;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.exception.RequestBodyException;
import io.katharsis.resource.mock.models.Memorandum;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.ComplexPojo;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.ResourceResponseContext;
import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourcePatchTest extends BaseControllerTest {

    private static final String REQUEST_TYPE = "PATCH";

    @Test
    public void onGivenRequestCollectionGetShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/");
        ResourcePatch sut = new ResourcePatch(resourceRegistry, typeParser, objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        Assert.assertEquals(result, false);
    }

    @Test
    public void onGivenRequestResourceGetShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/1");
        ResourcePatch sut = new ResourcePatch(resourceRegistry, typeParser, objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        Assert.assertEquals(result, true);
    }

    @Test
    public void onNoBodyResourceShouldThrowException() throws Exception {
        // GIVEN
        ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, objectMapper);

        // THEN
        expectedException.expect(RuntimeException.class);

        // WHEN
        sut.handle(new ResourcePath("fridges"), new QueryParamsAdapter(REQUEST_PARAMS), null, null);
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

        JsonPath taskPath = pathBuilder.buildPath("/tasks");

        // WHEN
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper);
        ResourceResponseContext taskResponse = resourcePost.handle(taskPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newTaskBody);
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
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/" + taskId);
        ResourcePatch sut = new ResourcePatch(resourceRegistry, typeParser, objectMapper);

        // WHEN
        BaseResponseContext response = sut.handle(jsonPath, new QueryParamsAdapter(REQUEST_PARAMS), null, taskPatch);

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

        JsonPath taskPath = pathBuilder.buildPath("/tasks");

        // WHEN
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper);
        ResourceResponseContext taskResponse = resourcePost.handle(taskPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newTaskBody);
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (taskResponse.getResponse().getEntity())).getId();
        assertThat(taskId).isNotNull();

        // GIVEN
        RequestBody taskPatch = new RequestBody();
        data = new DataBody();
        taskPatch.setData(data);
        data.setType("WRONG_AND_MISSING_TYPE");
        data.setAttributes(objectMapper.createObjectNode()
            .put("name", "task updated"));
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/" + taskId);
        ResourcePatch sut = new ResourcePatch(resourceRegistry, typeParser, objectMapper);

        // WHEN
        BaseResponseContext response = null;
        try {
            response = sut.handle(jsonPath, new QueryParamsAdapter(REQUEST_PARAMS), null, taskPatch);
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

        JsonPath documentsPath = pathBuilder.buildPath("/documents");

        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper);

        // WHEN
        ResourceResponseContext taskResponse = resourcePost.handle(documentsPath, new QueryParamsAdapter(REQUEST_PARAMS), null, memorandumBody);

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
        JsonPath documentPath = pathBuilder.buildPath("/documents/" + memorandumId);
        ResourcePatch sut = new ResourcePatch(resourceRegistry, typeParser, objectMapper);

        // WHEN
        BaseResponseContext memorandumResponse = sut.handle(documentPath, new QueryParamsAdapter(REQUEST_PARAMS), null, memorandumBody);

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

        JsonPath taskPath = pathBuilder.buildPath("/tasks");

        // WHEN
        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, objectMapper);
        ResourceResponseContext taskResponse = resourcePost.handle(taskPath, new QueryParamsAdapter(REQUEST_PARAMS), null, newTaskBody);
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
        JsonPath jsonPath = pathBuilder.buildPath("/tasks/" + taskId);
        ResourcePatch sut = new ResourcePatch(resourceRegistry, typeParser, objectMapper);

        // WHEN
        BaseResponseContext response = sut.handle(jsonPath, new QueryParamsAdapter(REQUEST_PARAMS), null, taskPatch);

        // THEN
        Assert.assertNotNull(response);
        assertThat(response.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        assertThat(((Task) (response.getResponse().getEntity())).getName()).isEqualTo("task updated");
        assertThat(((Task) (response.getResponse().getEntity())).getProject()).isNull();
    }

    @Test
    public void onGivenRequestResourcePatchShouldHandleMissingFields() throws Exception {

        JsonPath complexPojoPath = pathBuilder.buildPath("/complexpojos/1");

        // WHEN
        ResourceGet resourceGet = new ResourceGet(resourceRegistry, typeParser, includeFieldSetter);
        BaseResponseContext complexPojoResponse = resourceGet.handle(complexPojoPath, new QueryParamsAdapter(REQUEST_PARAMS), null, null);
        assertThat(complexPojoResponse.getResponse().getEntity()).isExactlyInstanceOf(ComplexPojo.class);
        Long complexPojoId = ((ComplexPojo) (complexPojoResponse.getResponse().getEntity())).getId();
        assertThat(complexPojoId).isNotNull();
        assertThat(((ComplexPojo) (complexPojoResponse.getResponse().getEntity())).getContainedPojo().getUpdateableProperty1()).isEqualTo("value from repository mock");

        // GIVEN
        RequestBody complexPojoPatch = new RequestBody();
        DataBody data = new DataBody();
        complexPojoPatch.setData(data);
        data.setType("complexpojos");

        String rawPatchData = "" +
                "{" +
                "  'containedPojo':{" +
                "    'updateableProperty1':'updated value'" +
                "  }," +
                "  'updateableProperty':'wasNullBefore'" +
                "}";
        rawPatchData = rawPatchData.replaceAll("'", "\"");

        JsonNode patchAttributes = objectMapper.readTree(rawPatchData);
        data.setAttributes(patchAttributes);
        JsonPath jsonPath = pathBuilder.buildPath("/complexpojos/" + complexPojoId);
        ResourcePatch sut = new ResourcePatch(resourceRegistry, typeParser, objectMapper);

        // WHEN
        BaseResponseContext response = sut.handle(jsonPath, new QueryParamsAdapter(REQUEST_PARAMS), null, complexPojoPatch);

        // THEN
        Assert.assertNotNull(response);
        assertThat(response.getResponse().getEntity()).isExactlyInstanceOf(ComplexPojo.class);
        assertThat(((ComplexPojo) (response.getResponse().getEntity())).getContainedPojo().getUpdateableProperty1()).isEqualTo("updated value");
        assertThat(((ComplexPojo) (response.getResponse().getEntity())).getContainedPojo().getUpdateableProperty2()).isEqualTo("value from repository mock");
        assertThat(((ComplexPojo) (response.getResponse().getEntity())).getUpdateableProperty()).isEqualTo("wasNullBefore");
    }

}

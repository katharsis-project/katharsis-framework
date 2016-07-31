package io.katharsis.dispatcher.controller.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.request.Request;
import io.katharsis.request.dto.DataBody;
import io.katharsis.request.dto.LinkageData;
import io.katharsis.request.dto.RequestBody;
import io.katharsis.request.dto.ResourceRelationships;
import io.katharsis.request.path.JsonApiPath;
import io.katharsis.resource.exception.ResourceException;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.mock.models.Memorandum;
import io.katharsis.resource.mock.models.OtherPojo;
import io.katharsis.resource.mock.models.Pojo;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.ProjectData;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.User;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.HttpStatus;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;

import static io.katharsis.dispatcher.controller.HttpMethod.POST;
import static org.assertj.core.api.Assertions.assertThat;

public class ResourcePostTest extends BaseControllerTest {

    @Test
    public void onGivenRequestCollectionGetShouldDenyIt() {
        // GIVEN
        JsonApiPath jsonApiPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks/1");
        Request request = new Request(jsonApiPath, POST.name(), null, parameterProvider);

        ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        Assert.assertEquals(result, true);
    }

    @Test
    public void onGivenRequestResourceGetShouldAcceptIt() {
        // GIVEN
        JsonApiPath jsonApiPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks");
        Request request = new Request(jsonApiPath, POST.name(), null, parameterProvider);

        ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        boolean result = sut.isAcceptable(request);

        // THEN
        Assert.assertEquals(result, true);
    }

    @Test
    public void onInconsistentResourceTypesShouldThrowException() throws Exception {
        // GIVEN
        RequestBody newProjectBody = fixtureSampleProjectBody();

        JsonApiPath jsonApiPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks");
        Request request = new Request(jsonApiPath, POST.name(), serialize(newProjectBody), parameterProvider);

        ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // THEN
        expectedException.expect(RuntimeException.class);

        // WHEN
        sut.handle(request);
    }

    private RequestBody fixtureSampleProjectBody() {
        DataBody data = new DataBody();
        data.setType("projects");
        data.setAttributes(objectMapper.createObjectNode().put("name", "sample task"));
        return new RequestBody(data);
    }

    @Test
    public void onNonExistentResourceShouldThrowException() throws Exception {
        // GIVEN
        RequestBody newProjectBody = new RequestBody();
        DataBody data = new DataBody();
        newProjectBody.setData(data);
        data.setType("fridges");

        JsonApiPath jsonApiPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/fridges");
        Request request = new Request(jsonApiPath, POST.name(), null, parameterProvider);

        ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // THEN
        expectedException.expect(ResourceNotFoundException.class);

        // WHEN
        sut.handle(request);
    }

    @Test
    public void onNoBodyResourceShouldThrowException() throws Exception {
        // GIVEN
        JsonApiPath jsonApiPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/fridges");
        Request request = new Request(jsonApiPath, POST.name(), null, parameterProvider);

        ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // THEN
        expectedException.expect(RuntimeException.class);

        // WHEN
        sut.handle(request);
    }

    @Test
    public void onNewResourcesAndRelationshipShouldPersistThoseData() throws Exception {
        // GIVEN
        RequestBody newProjectBody = fixtureProjectBodyWithAttributes();
        JsonApiPath jsonApiPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/projects");
        Request request = new Request(jsonApiPath, POST.name(), serialize(newProjectBody), parameterProvider);

        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        BaseResponseContext projectResponse = resourcePost.handle(request);

        // THEN
        assertThat(projectResponse.getHttpStatus()).isEqualTo(HttpStatus.CREATED_201);
        assertThat(projectResponse.getResponse().getEntity()).isExactlyInstanceOf(Project.class);
        Project persistedProject = (Project) (projectResponse.getResponse().getEntity());
        assertThat(persistedProject.getId()).isNotNull();
        assertThat(persistedProject.getName()).isEqualTo("sample project");
        assertThat(persistedProject.getData()).isEqualToComparingFieldByField(new ProjectData().setData("asd"));
        Long projectId = ((Project) (projectResponse.getResponse().getEntity())).getId();

        /* ------- */

        // GIVEN
        RequestBody newTaskBody = fixtureProjectBodyWIthRelationship(projectId);

        jsonApiPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/tasks");
        request = new Request(jsonApiPath, POST.name(), serialize(newTaskBody), parameterProvider);

        // WHEN
        BaseResponseContext taskResponse = resourcePost.handle(request);

        // THEN
        assertThat(taskResponse.getHttpStatus()).isEqualTo(HttpStatus.CREATED_201);
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(Task.class);
        Long taskId = ((Task) (taskResponse.getResponse().getEntity())).getId();
        assertThat(taskId).isNotNull();
        assertThat(((Task) (taskResponse.getResponse().getEntity())).getName()).isEqualTo("sample task");

    }

    private RequestBody fixtureProjectBodyWIthRelationship(Long projectId) {
        DataBody data = new DataBody();
        data.setType("tasks");
        data.setAttributes(objectMapper.createObjectNode().put("name", "sample task"));
        data.setRelationships(new ResourceRelationships());
        data.getRelationships().setAdditionalProperty("project", new LinkageData("projects", projectId.toString()));
        return new RequestBody(data);
    }

    private RequestBody fixtureProjectBodyWithAttributes() {
        ObjectNode attributes = objectMapper.createObjectNode().put("name", "sample project");
        attributes.putObject("data").put("data", "asd");

        DataBody data = DataBody.builder()
                .type("projects")
                .attributes(attributes)
                .build();
        return new RequestBody(data);
    }

    @Test
    @Ignore
    //TODO: ieugen: fails
    public void onNewResourcesAndRelationshipsShouldPersistThoseData() throws Exception {
        // GIVEN
        RequestBody newProjectBody = fixtureProjectBody();
        JsonApiPath jsonApiPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/projects");
        Request request = new Request(jsonApiPath, POST.name(), serialize(newProjectBody), parameterProvider);

        ResourcePost resourcePost = new ResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        BaseResponseContext projectResponse = resourcePost.handle(request);

        // THEN
        assertThat(projectResponse.getResponse().getEntity()).isExactlyInstanceOf(Project.class);
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getId()).isNotNull();
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getName()).isEqualTo("sample project");
        Long projectId = ((Project) (projectResponse.getResponse().getEntity())).getId();

        /* ------- */

        // GIVEN
        RequestBody newUserBody = fixtureNewUserBodyWithProjectRelation(projectId);

        jsonApiPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/users");
        request = new Request(jsonApiPath, POST.name(), serialize(newUserBody), parameterProvider);

        // WHEN
        BaseResponseContext taskResponse = resourcePost.handle(request);

        // THEN
        assertThat(taskResponse.getResponse().getEntity()).isExactlyInstanceOf(User.class);
        Long userId = ((User) (taskResponse.getResponse().getEntity())).getId();
        assertThat(userId).isNotNull();
        assertThat(((User) (taskResponse.getResponse().getEntity())).getName()).isEqualTo("some user");

        assertThat(((User) (taskResponse.getResponse().getEntity())).getAssignedProjects()).hasSize(1);
        assertThat(((User) (taskResponse.getResponse().getEntity())).getAssignedProjects().get(0).getId()).isEqualTo(projectId);
    }

    private RequestBody fixtureNewUserBodyWithProjectRelation(Long projectId) {
        JsonNode attributes = objectMapper.createObjectNode().put("name", "some user");
        ResourceRelationships resourceRelationships = new ResourceRelationships();
        resourceRelationships.setAdditionalProperty("assignedProjects",
                Collections.singletonList(new LinkageData("projects", projectId.toString())));

        return new RequestBody(new DataBody(null, "users", resourceRelationships, attributes));
    }

    @Test
    public void onNewInheritedResourceShouldPersistThisResource() throws Exception {
        // GIVEN
        RequestBody newMemorandumBody = fixtureMemorandumBody();

        JsonApiPath jsonApiPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/documents");
        Request request = new Request(jsonApiPath, POST.name(), serialize(newMemorandumBody), parameterProvider);

        ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        BaseResponseContext memorandumResponse = sut.handle(request);

        // THEN
        assertThat(memorandumResponse.getResponse().getEntity()).isExactlyInstanceOf(Memorandum.class);
        Memorandum persistedMemorandum = (Memorandum) (memorandumResponse.getResponse().getEntity());
        assertThat(persistedMemorandum.getId()).isNotNull();
        assertThat(persistedMemorandum.getTitle()).isEqualTo("sample title");
        assertThat(persistedMemorandum.getBody()).isEqualTo("sample body");
    }

    private RequestBody fixtureMemorandumBody() {
        ObjectNode attributes = objectMapper.createObjectNode()
                .put("title", "sample title")
                .put("body", "sample body");

        DataBody data = new DataBody();
        data.setType("memoranda");
        data.setAttributes(attributes);
        return new RequestBody(data);
    }

    @Test
    @Ignore
    //TODO: ieugen: fails
    public void onResourceWithCustomNamesShouldSaveParametersCorrectly() throws Exception {
        // GIVEN - creating sample project id
        RequestBody newProjectBody = fixtureProjectBody();

        JsonApiPath jsonApiPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/projects");
        Request request = new Request(jsonApiPath, POST.name(), serialize(newProjectBody), parameterProvider);

        ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        BaseResponseContext projectResponse = sut.handle(request);

        // THEN
        assertThat(projectResponse.getResponse().getEntity()).isExactlyInstanceOf(Project.class);
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getId()).isNotNull();
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getName()).isEqualTo("sample project");
        Long projectId = ((Project) (projectResponse.getResponse().getEntity())).getId();

        /* ------- */

        // GIVEN
        RequestBody pojoBody = fixtureRequestBodyWIthRelationshipAndAttributes(projectId);

        jsonApiPath = JsonApiPath.parsePathFromStringUrl("http://domain.local/pojo");
        request = new Request(jsonApiPath, POST.name(), serialize(pojoBody), parameterProvider);

        // WHEN
        BaseResponseContext pojoResponse = sut.handle(request);

        // THEN
        assertThat(pojoResponse.getResponse().getEntity()).isExactlyInstanceOf(Pojo.class);
        Pojo persistedPojo = (Pojo) (pojoResponse.getResponse().getEntity());
        assertThat(persistedPojo.getId()).isNotNull();
        assertThat(persistedPojo.getOtherPojo()).isEqualTo(new OtherPojo().setValue("hello"));
        assertThat(persistedPojo.getProject()).isNotNull();
        assertThat(persistedPojo.getProject().getId()).isEqualTo(projectId);
        assertThat(persistedPojo.getProjects()).hasSize(1);
        assertThat(persistedPojo.getProjects().get(0).getId()).isEqualTo(projectId);
    }

    private RequestBody fixtureRequestBodyWIthRelationshipAndAttributes(Long projectId) {
        ResourceRelationships relationships = new ResourceRelationships();
        relationships.setAdditionalProperty("some-project", new LinkageData("projects", Long.toString(projectId)));
        relationships.setAdditionalProperty("some-projects",
                Collections.singletonList(new LinkageData("projects", Long.toString(projectId))));

        JsonNode jsonNode = objectMapper.createObjectNode().put("value", "hello");
        JsonNode attributes = objectMapper.createObjectNode().set("other-pojo", jsonNode);

        return new RequestBody(new DataBody(null, "pojo", relationships, attributes));
    }

    private RequestBody fixtureProjectBody() {
        DataBody data = new DataBody();
        data.setType("projects");
        data.setAttributes(objectMapper.createObjectNode().put("name", "sample project"));
        return new RequestBody(data);
    }

    @Test
    public void onResourceWithInvalidRelationshipNameShouldThrowException() throws Exception {
        // GIVEN - creating sample project id
        RequestBody newProjectBody = new RequestBody();
        DataBody data = new DataBody();
        newProjectBody.setData(data);
        data.setType("projects");
        data.setAttributes(objectMapper.createObjectNode().put("name", "sample project"));

        JsonApiPath projectPath = JsonApiPath.parsePathFromStringUrl("http://local/projects");
        Request request = new Request(projectPath, "POST", serialize(newProjectBody), parameterProvider);
        ResourcePost sut = new ResourcePost(resourceRegistry, typeParser, queryParamsBuilder, objectMapper);

        // WHEN
        BaseResponseContext projectResponse = sut.handle(request);

        // THEN
        assertThat(projectResponse.getResponse().getEntity()).isExactlyInstanceOf(Project.class);
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getId()).isNotNull();
        assertThat(((Project) (projectResponse.getResponse().getEntity())).getName()).isEqualTo("sample project");
        Long projectId = ((Project) (projectResponse.getResponse().getEntity())).getId();

        /* ------- */

        // GIVEN
        RequestBody pojoBody = new RequestBody();
        DataBody pojoData = new DataBody();
        pojoBody.setData(pojoData);
        pojoData.setType("pojo");
        JsonNode put = objectMapper.createObjectNode().put("value", "hello");
        JsonNode attributes = objectMapper.createObjectNode()
                .set("other-pojo", put);
        pojoData.setAttributes(attributes);
        ResourceRelationships relationships = new ResourceRelationships();
        String invalidRelationshipName = "invalid-relationship";
        relationships.setAdditionalProperty(invalidRelationshipName, new LinkageData("projects", Long.toString(projectId)));
        pojoData.setRelationships(relationships);

        JsonApiPath pojoPath = JsonApiPath.parsePathFromStringUrl("http://local/pojo");
        Request request1 = new Request(pojoPath, "POST", serialize(pojoBody), parameterProvider);
        // THEN
        expectedException.expect(ResourceException.class);
        expectedException.expectMessage(String.format("Invalid relationship name: %s", invalidRelationshipName));

        // WHEN
        sut.handle(request1);
    }
}

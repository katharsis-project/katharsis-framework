package io.katharsis.dispatcher.controller.resource;

import io.katharsis.dispatcher.controller.BaseControllerTest;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.queryspec.internal.QuerySpecAdapter;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.User;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.repository.adapter.RelationshipRepositoryAdapter;
import io.katharsis.resource.registry.repository.adapter.ResourceRepositoryAdapter;
import io.katharsis.response.BaseResponseContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class FieldResourceGetTest extends BaseControllerTest {
    private static final String REQUEST_TYPE = "GET";

    @Test
    public void onValidRequestShouldAcceptIt() {
        // GIVEN
        JsonPath jsonPath = pathBuilder.buildPath("tasks/1/project");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        IncludeLookupSetter includeFieldSetter = mock(IncludeLookupSetter.class);
        FieldResourceGet sut = new FieldResourceGet(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    public void onRelationshipRequestShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = new ResourcePath("tasks/1/relationships/project");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        IncludeLookupSetter includeFieldSetter = mock(IncludeLookupSetter.class);
        FieldResourceGet sut = new FieldResourceGet(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onNonRelationRequestShouldDenyIt() {
        // GIVEN
        JsonPath jsonPath = new ResourcePath("tasks");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        IncludeLookupSetter includeFieldSetter = mock(IncludeLookupSetter.class);
        FieldResourceGet sut = new FieldResourceGet(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

        // THEN
        assertThat(result).isFalse();
    }

    @Test
    public void onGivenRequestFieldResourceGetShouldHandleIt() throws Exception {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/tasks/1/project");
        FieldResourceGet sut = new FieldResourceGet(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        BaseResponseContext response = sut.handle(jsonPath, new QueryParamsAdapter(new QueryParams()), null, null);

        // THEN
        Assert.assertNotNull(response);
    }

    @Test
    public void onGivenRequestFieldResourcesGetShouldHandleIt() throws Exception {
        // GIVEN

        JsonPath jsonPath = pathBuilder.buildPath("/users/1/assignedProjects");
        FieldResourceGet sut = new FieldResourceGet(resourceRegistry, typeParser, includeFieldSetter);

        // WHEN
        BaseResponseContext response = sut.handle(jsonPath, new QuerySpecAdapter(new QuerySpec(Project.class), resourceRegistry), null, null);

        // THEN
        Assert.assertNotNull(response);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void onGivenIncludeRequestFieldResourcesGetShouldHandleIt() throws Exception {

        // get repositories
        ResourceRepositoryAdapter userRepo = resourceRegistry.getEntry(User.class).getResourceRepository(null);
        ResourceRepositoryAdapter projectRepo = resourceRegistry.getEntry(Project.class).getResourceRepository(null);
        ResourceRepositoryAdapter taskRepo = resourceRegistry.getEntry(Task.class).getResourceRepository(null);

        RelationshipRepositoryAdapter relRepositoryUserToProject = resourceRegistry.getEntry(User.class)
                .getRelationshipRepositoryForClass(Project.class, null);
        RelationshipRepositoryAdapter relRepositoryProjectToTask = resourceRegistry.getEntry(Project.class)
                .getRelationshipRepositoryForClass(Task.class, null);
        // setup test data
        User user = new User();
        user.setId(1L);
        userRepo.create(user, null);
        Project project = new Project();
        project.setId(2L);
        projectRepo.create(project, null);
        Task task = new Task();
        task.setId(3L);
        taskRepo.create(task, null);
        relRepositoryUserToProject.setRelations(user, Collections.singletonList(project.getId()), "assignedProjects", null);
        relRepositoryProjectToTask.setRelation(project, task.getId(), "includedTask", null);

        Map<String, Set<String>> params = new HashMap<String, Set<String>>();
        addParams(params, "include[projects]", "includedTask");
        QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
        QueryAdapter queryAdapter = new QueryParamsAdapter(Project.class, queryParams, resourceRegistry);
        JsonPath jsonPath = pathBuilder.buildPath("/users/1/assignedProjects");
        FieldResourceGet sut = new FieldResourceGet(resourceRegistry, typeParser, includeFieldSetter);

        BaseResponseContext response = sut.handle(jsonPath, queryAdapter, null, null);

        // THEN
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponse().getEntity());
        Assert.assertEquals(LinkedList.class, response.getResponse().getEntity().getClass());
        LinkedList entityList = ((LinkedList) response.getResponse().getEntity());
        Assert.assertEquals(true, entityList.size() > 0);
        Assert.assertEquals(Project.class, entityList.get(0).getClass());
        Project returnedProject = (Project) entityList.get(0);
        Assert.assertEquals(project.getId(), returnedProject.getId());
        Assert.assertNotNull(returnedProject.getIncludedTask());
        Assert.assertEquals(task.getId(), returnedProject.getIncludedTask().getId());
    }


}

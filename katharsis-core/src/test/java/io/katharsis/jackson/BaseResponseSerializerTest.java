package io.katharsis.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.katharsis.errorhandling.ErrorData;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.request.path.FieldPath;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.ProjectEager;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.User;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.response.BaseResponseContext;
import io.katharsis.response.CollectionResponseContext;
import io.katharsis.response.Container;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.LinkageContainer;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;
import io.katharsis.response.ResourceResponseContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

public class BaseResponseSerializerTest extends BaseSerializerTest {

    private static final QueryAdapter REQUEST_PARAMS = new QueryParamsAdapter(new QueryParams());

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
        String result = sut
                .writeValueAsString(new ResourceResponseContext(buildResponse(task), new ResourcePath("projects"), REQUEST_PARAMS));

        // THEN
        assertThatJson(result).node("data").isPresent();
        assertThatJson(result).node("data.id").isEqualTo("\"1\"");
        assertThatJson(result).node("included").isArray().ofLength(1);
        assertThatJson(result).node("included[0].id").isEqualTo("\"2\"");
    }

    @Test
    public void onNestedNonAnnotatedResourcesShouldReturnNestedId() throws JsonProcessingException {
        // GIVEN
        Task task = new Task();
        task.setId(1L);

        Task otherTask = new Task();
        otherTask.setId(2L);
        otherTask.setName("other task");

        task.setOtherTasks(Collections.singletonList(otherTask));

        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
        QueryParams queryParams = queryParamsBuilder.buildQueryParams(Collections.<String, Set<String>>emptyMap());
        JsonPath jsonPath = new PathBuilder(resourceRegistry).buildPath("/tasks");

        // WHEN
        String result = sut.writeValueAsString(new Container(task, new ResourceResponseContext(null, jsonPath, new QueryParamsAdapter(queryParams))));

        // THEN
        assertThatJson(result).node("id").isEqualTo("\"1\"");
        assertThatJson(result).node("attributes.otherTasks").isArray().ofLength(1);
        assertThatJson(result).node("attributes.otherTasks[0].name").isEqualTo("other task");
        assertThatJson(result).node("attributes.otherTasks[0].id").isEqualTo(2);
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
        String result = sut
                .writeValueAsString(new ResourceResponseContext(buildResponse(user), new ResourcePath("projects"), REQUEST_PARAMS));

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
        String result = sut.writeValueAsString(new CollectionResponseContext(buildResponse(Arrays.asList(task1, task2)),
                new ResourcePath("tasks"), REQUEST_PARAMS));

        // THEN
        assertThatJson(result).node("data").isArray().ofLength(2);
    }

    @Test
    public void onSingleResponseWithManyLinkagesShouldReturnArrayOfLinks() throws Exception {
        // GIVEN
        RegistryEntry entry = resourceRegistry.getEntry(Task.class);

        LinkageContainer linkageContainer1 = new LinkageContainer(new Task().setId(1L), Task.class, entry);
        LinkageContainer linkageContainer2 = new LinkageContainer(new Task().setId(2L), Task.class, entry);

        // WHEN
        JsonApiResponse response = buildResponse(Arrays.asList(linkageContainer1, linkageContainer2));
        String result = sut.writeValueAsString(new CollectionResponseContext(response, new ResourcePath("tasks"), REQUEST_PARAMS));

        // THEN
        assertThatJson(result).node("data").isArray().ofLength(2);
        assertThatJson(result).node("data[0].type").isEqualTo("tasks");
    }

    @Test
    public void onSingleResponseWithOneLinkagesShouldReturnOneLink() throws Exception {
        // GIVEN
        RegistryEntry entry = resourceRegistry.getEntry(Task.class);

        LinkageContainer linkageContainer1 = new LinkageContainer(new Task().setId(1L), Task.class, entry);

        // WHEN
        String result = sut.writeValueAsString(new ResourceResponseContext(buildResponse(linkageContainer1),
                new ResourcePath("tasks"), REQUEST_PARAMS));

        // THEN
        assertThatJson(result).node("data.id").isStringEqualTo("1");
        assertThatJson(result).node("data.type").isEqualTo("tasks");
    }

    @Test
    public void onSingleResponseWithNoResourcesShouldReturnEmptyArray() throws Exception {
        // WHEN
        String result = sut
                .writeValueAsString(new CollectionResponseContext(new JsonApiResponse(), new ResourcePath("projects"), REQUEST_PARAMS));

        // THEN
        assertThatJson(result).node("data").isArray().ofLength(0);
    }

    @Test
    public void onSingleResponseWithNoResourceShouldReturnNull() throws Exception {
        // WHEN
        String result = sut
                .writeValueAsString(new ResourceResponseContext(new JsonApiResponse(), new ResourcePath("projects"), REQUEST_PARAMS));

        // THEN
        assertThatJson(result).node("data").isEqualTo(null);
    }

    @Test
    public void onMetaInformationShouldReturnMetaObject() throws Exception {
        // GIVEN
        JsonApiResponse response = new JsonApiResponse()
                .setMetaInformation(new MetaData("Humpty Dumpty"));

        // WHEN
        String result = sut.writeValueAsString(
                new ResourceResponseContext(response, new ResourcePath("projects"), REQUEST_PARAMS));

        // THEN
        assertThatJson(result).node("meta.author").isEqualTo("Humpty Dumpty");
    }

    @Test
    public void onLinksInformationShouldReturnLinksObject() throws Exception {
        // GIVEN
        JsonApiResponse response = new JsonApiResponse()
                .setLinksInformation(new LinksData("/sth/123"));

        // WHEN
        String result = sut.writeValueAsString(
                new ResourceResponseContext(response, new ResourcePath("projects"), REQUEST_PARAMS));

        // THEN
        assertThatJson(result).node("links.self").isEqualTo("/sth/123");
    }

    @Test
    public void onNoMetaInformationShouldReturnNoMetaObject() throws Exception {
        // WHEN
        String result = sut.writeValueAsString(
                new ResourceResponseContext(new JsonApiResponse(), new ResourcePath("projects"), REQUEST_PARAMS));

        // THEN
        assertThatJson(result).node("meta").isAbsent();
    }

    @Test
    public void onNoLinksInformationShouldReturnNoLinksObject() throws Exception {
        // WHEN
        String result = sut.writeValueAsString(
                new ResourceResponseContext(new JsonApiResponse(), new ResourcePath("projects"), REQUEST_PARAMS));

        // THEN
        assertThatJson(result).node("links").isAbsent();
    }

    @Test
    public void onErrorsShouldReturnErrorsObject() throws Exception {
        // GIVEN
        JsonApiResponse response = new JsonApiResponse()
                .setErrors(Collections.singleton(ErrorData.builder().build()));

        // WHEN
        String result = sut.writeValueAsString(
                new ResourceResponseContext(response, new ResourcePath("projects"), REQUEST_PARAMS));

        // THEN
        assertThatJson(result).node("errors").isPresent();
    }

    @Test
    public void onNoErrorsShouldReturnNoErrorsObject() throws Exception {
        // WHEN
        String result = sut.writeValueAsString(
                new ResourceResponseContext(new JsonApiResponse(), new ResourcePath("projects"), REQUEST_PARAMS));

        // THEN
        assertThatJson(result).node("errors").isAbsent();
    }

    @Test
    public void onCustomResponseShouldThrowException() throws Exception {
        // THEN
        expectedException.expect(JsonMappingException.class);

        // WHEN
        sut.writeValueAsString(new BaseResponseContext() {
            @Override
            public int getHttpStatus() {
                return 0;
            }

            @Override
            public JsonApiResponse getResponse() {
                return null;
            }

            @Override
            public JsonPath getJsonPath() {
                return null;
            }

            @Override
            public QueryAdapter getQueryAdapter() {
                return null;
            }
        });
    }

    @Test
    public void onDoubledIncludedResourcesShouldReturnUniqueValues() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setId(2L);
        project.setName("Sample project");
        Task task = new Task();
        task.setId(1L);
        task.setName("Sample task");
        task.setProject(project);
        task.setProjects(Collections.singletonList(project));

        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
        QueryParams queryParams = queryParamsBuilder.buildQueryParams(
                Collections.singletonMap("include[tasks]", Collections.singleton("projects")));

        // WHEN
        String result = sut
                .writeValueAsString(new ResourceResponseContext(buildResponse(task), new ResourcePath("projects"), new QueryParamsAdapter(queryParams)));

        // THEN
        assertThatJson(result).node("data").isPresent();
        assertThatJson(result).node("data.id").isEqualTo("\"1\"");
        assertThatJson(result).node("included").isArray().ofLength(1);
        assertThatJson(result).node("included[0].id").isEqualTo("\"2\"");
    }


    @Test
    public void onFieldResourcesWithQueryParamsShouldReturnIncludedValues() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setId(1L);
        project.setName("Sample project");

        Task task1 = new Task();
        task1.setId(1L);
        task1.setName("Sample task 1");
        Task task2 = new Task();
        task2.setId(2L);
        task2.setName("Sample task 2");
        project.setTasks(Arrays.asList(task1, task2));

        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
        QueryParams queryParams = queryParamsBuilder.buildQueryParams(
                Collections.singletonMap("include[projects]", Collections.singleton("tasks")));

        // WHEN
        String result = sut
                .writeValueAsString(new ResourceResponseContext(buildResponse(project), new FieldPath("projects"), new QueryParamsAdapter(queryParams)));

        // THEN
        assertThatJson(result).node("data").isPresent();
        assertThatJson(result).node("data.id").isEqualTo("\"1\"");
        assertThatJson(result).node("data.relationships.tasks").isPresent();
        assertThatJson(result).node("data.relationships.tasks.data").isArray().ofLength(2);
        assertThatJson(result).node("included").isArray().ofLength(2);
        assertThatJson(result).node("included[0].type").isEqualTo("tasks");
    }

    @Test
    public void onFieldResourcesWithQueryParamsShouldReturnIncludedValue() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setId(1L);
        project.setName("Sample project");

        Task task = new Task();
        task.setId(1L);
        task.setName("Sample task");
        project.setTask(task);
        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
        QueryParams queryParams = queryParamsBuilder.buildQueryParams(
                Collections.singletonMap("include[projects]", Collections.singleton("task")));

        // WHEN
        String result = sut
                .writeValueAsString(new ResourceResponseContext(buildResponse(project), new FieldPath("projects"), new QueryParamsAdapter(queryParams)));

        // THEN
        assertThatJson(result).node("data").isPresent();
        assertThatJson(result).node("data.id").isEqualTo("\"1\"");
        assertThatJson(result).node("data.relationships.tasks").isPresent();
        assertThatJson(result).node("data.relationships.task.data.type").isEqualTo("tasks");
        assertThatJson(result).node("included").isArray().ofLength(1);
        assertThatJson(result).node("included[0].type").isEqualTo("tasks");
    }

    @Test
    public void onFieldResourcesShouldNotReturnIncludedValues() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setId(1L);
        project.setName("Sample project");

        Task task1 = new Task();
        task1.setId(1L);
        task1.setName("Sample task1");
        Task task2 = new Task();
        task2.setId(2L);
        task2.setName("Sample task2");
        project.setTasks(Arrays.asList(task1, task2));

        // WHEN
        String result = sut
                .writeValueAsString(new ResourceResponseContext(buildResponse(project), new FieldPath("project"), new QueryParamsAdapter(new QueryParams())));

        // THEN
        assertThatJson(result).node("data").isPresent();
        assertThatJson(result).node("data.id").isEqualTo("\"1\"");
        assertThatJson(result).node("data.relationships.tasks").isPresent();
        assertThatJson(result).node("data.relationships.tasks.data").isAbsent();
        assertThatJson(result).node("included").isArray().ofLength(0);
    }

    @Test
    public void onEagerFieldResourcesShouldReturnIncludedValues() throws Exception {
        // GIVEN
        ProjectEager projectEager = new ProjectEager();
        projectEager.setId(1L);
        projectEager.setName("Sample project");

        Task task1 = new Task();
        task1.setId(1L);
        task1.setName("Sample task1");
        Task task2 = new Task();
        task2.setId(2L);
        task2.setName("Sample task2");
        projectEager.setTasks(Arrays.asList(task1, task2));

        // WHEN
        String result = sut
                .writeValueAsString(new ResourceResponseContext(buildResponse(projectEager), new FieldPath("project"), new QueryParamsAdapter(new QueryParams())));

        // THEN
        assertThatJson(result).node("data").isPresent();
        assertThatJson(result).node("data.id").isEqualTo("\"1\"");
        assertThatJson(result).node("data.relationships.tasks").isPresent();
        assertThatJson(result).node("data.relationships.tasks.data").isArray().ofLength(2);
        assertThatJson(result).node("included").isArray().ofLength(2);
        assertThatJson(result).node("included[0].type").isEqualTo("tasks");
    }

    public static class MetaData implements MetaInformation {
        private String author;

        public MetaData(String author) {
            this.author = author;
        }

        public String getAuthor() {
            return author;
        }
    }

    public static class LinksData implements LinksInformation {
        private String self;

        public LinksData(String self) {
            this.self = self;
        }

        public String getSelf() {
            return self;
        }
    }
}

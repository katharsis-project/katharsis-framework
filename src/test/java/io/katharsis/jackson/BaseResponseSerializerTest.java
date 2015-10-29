package io.katharsis.jackson;

import com.fasterxml.jackson.databind.JsonMappingException;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.ResourcePath;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.User;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.response.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

public class BaseResponseSerializerTest extends BaseSerializerTest {

    private static final QueryParams REQUEST_PARAMS = new QueryParams();

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
            .writeValueAsString(new ResourceResponse(task, new ResourcePath("projects"), REQUEST_PARAMS, null, null));

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
        String result = sut
            .writeValueAsString(new ResourceResponse(user, new ResourcePath("projects"), REQUEST_PARAMS, null, null));

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
        String result = sut.writeValueAsString(new CollectionResponse(Arrays.asList(task1, task2),
            new ResourcePath("tasks"), REQUEST_PARAMS, null, null));

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
        String result = sut.writeValueAsString(new CollectionResponse(Arrays.asList(linkageContainer1, linkageContainer2),
            new ResourcePath("tasks"), REQUEST_PARAMS, null, null));

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
        String result = sut.writeValueAsString(new ResourceResponse(linkageContainer1,
            new ResourcePath("tasks"), REQUEST_PARAMS, null, null));

        // THEN
        assertThatJson(result).node("data.id").isStringEqualTo("1");
        assertThatJson(result).node("data.type").isEqualTo("tasks");
    }

    @Test
    public void onSingleResponseWithNoResourcesShouldReturnEmptyArray() throws Exception {
        // WHEN
        String result = sut
            .writeValueAsString(new CollectionResponse(null, new ResourcePath("projects"), REQUEST_PARAMS, null, null));

        // THEN
        assertThatJson(result).node("data").isArray().ofLength(0);
    }

    @Test
    public void onSingleResponseWithNoResourceShouldReturnNull() throws Exception {
        // WHEN
        String result = sut
            .writeValueAsString(new ResourceResponse(null, new ResourcePath("projects"), REQUEST_PARAMS, null, null));

        // THEN
        assertThatJson(result).node("data").isEqualTo(null);
    }

    @Test
    public void onMetaInformationShouldReturnMetaObject() throws Exception {
        // WHEN
        String result = sut.writeValueAsString(
            new ResourceResponse(null, new ResourcePath("projects"), REQUEST_PARAMS, new MetaData("Humpty Dumpty"), null));

        // THEN
        assertThatJson(result).node("meta.author").isEqualTo("Humpty Dumpty");
    }

    @Test
    public void onLinksInformationShouldReturnLinksObject() throws Exception {
        // WHEN
        String result = sut.writeValueAsString(
            new ResourceResponse(null, new ResourcePath("projects"), REQUEST_PARAMS, null, new LinksData("/sth/123")));

        // THEN
        assertThatJson(result).node("links.self").isEqualTo("/sth/123");
    }

    @Test
    public void onNoMetaInformationShouldReturnNoMetaObject() throws Exception {
        // WHEN
        String result = sut.writeValueAsString(
            new ResourceResponse(null, new ResourcePath("projects"), REQUEST_PARAMS, null, null));

        // THEN
        assertThatJson(result).node("meta").isAbsent();
    }

    @Test
    public void onNoLinksInformationShouldReturnNoLinksObject() throws Exception {
        // WHEN
        String result = sut.writeValueAsString(
            new ResourceResponse(null, new ResourcePath("projects"), REQUEST_PARAMS, null, null));

        // THEN
        assertThatJson(result).node("links").isAbsent();
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
            public QueryParams getQueryParams() {
                return null;
            }

            @Override
            public MetaInformation getMetaInformation() {
                return null;
            }

            @Override
            public LinksInformation getLinksInformation() {
                return null;
            }
        });
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

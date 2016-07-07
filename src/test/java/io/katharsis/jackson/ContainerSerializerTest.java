package io.katharsis.jackson;

import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.path.JsonPath;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.resource.mock.models.OtherPojo;
import io.katharsis.resource.mock.models.Pojo;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.models.User;
import io.katharsis.response.Container;
import io.katharsis.response.HttpStatus;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.LinksInformation;
import io.katharsis.response.MetaInformation;
import io.katharsis.response.ResourceResponseContext;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

public class ContainerSerializerTest extends BaseSerializerTest {

    @Test
    public void onSimpleObjectShouldIncludeType() throws Exception {
        // GIVEN
        Project project = new Project();

        // WHEN
        String result = sut.writeValueAsString(new Container(project, testResponse));

        // THEN
        assertThatJson(result).node("type").isEqualTo("projects");
    }

    @Test
    public void onSimpleObjectShouldIncludeStringId() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setId(1L);

        // WHEN
        String result = sut.writeValueAsString(new Container(project, testResponse));

        // THEN
        assertThatJson(result).node("id").isEqualTo("\"1\"");
    }

    @Test
    public void onSimpleObjectShouldIncludeAttributes() throws Exception {
        // GIVEN
        User user = new User();
        user.setName("name");
        user.setMetaInformation(new MetaInformation() {
            public long getCount() {
                return 42;
            }
        });
        user.setLinksInformation(new LinksInformation() {
            public String getSpaceBubble() {
                return "value";
            }
        });

        // WHEN
        String result = sut.writeValueAsString(new Container(user, testResponse));

        // THEN
        assertThatJson(result).node("attributes.name").isEqualTo("name");
        assertThatJson(result).node("attributes.metaInformation").isAbsent();
        assertThatJson(result).node("attributes.linksInformation").isAbsent();
    }

    @Test
    public void onSimpleObjectWithNullValueShouldNotIncludeAttributes() throws Exception {
        // GIVEN
        Project project = new Project();

        // WHEN
        String result = sut.writeValueAsString(new Container(project, testResponse));

        // THEN
        assertThatJson(result).node("attributes.name").isAbsent();
    }

    @Test
    public void onNullQueryParamsShouldSerializeCorrectly() throws Exception {
        // GIVEN
        Project project = new Project();

        // WHEN
        String result = sut.writeValueAsString(new Container(project, new ResourceResponseContext(buildResponse(null), HttpStatus.NO_CONTENT_204)));

        // THEN
        assertThatJson(result).isPresent();
    }

    @Test
    public void onIncludedFieldsInParamsShouldContainIncludedList() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setName("name");
        project.setDescription("description");

        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
        QueryParams queryParams = queryParamsBuilder.buildQueryParams(
            Collections.singletonMap("fields[projects]", Collections.singleton("name")));
        JsonPath jsonPath = new PathBuilder(resourceRegistry).buildPath("/projects");

        // WHEN
        String result = sut.writeValueAsString(new Container(project, new ResourceResponseContext(new JsonApiResponse(), jsonPath, queryParams)));

        // THEN
        assertThatJson(result).node("attributes.name").isEqualTo("name");
        assertThatJson(result).node("attributes.description").isAbsent();
    }

    @Test
    public void onIncludedRelationshipInParamsShouldContainIncludedList() throws Exception {
        // GIVEN
        Task task = new Task();
        task.setName("some name");
        Project project = new Project();
        project.setId(1L);
        task.setProject(project);

        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
        QueryParams queryParams = queryParamsBuilder.buildQueryParams(
            Collections.singletonMap("fields[tasks]", Collections.singleton("project")));
        JsonPath jsonPath = new PathBuilder(resourceRegistry).buildPath("/tasks");

        // WHEN
        String result = sut.writeValueAsString(new Container(task, new ResourceResponseContext(new JsonApiResponse(), jsonPath, queryParams)));

        // THEN
        assertThatJson(result).node("relationships.project").isPresent();
        assertThatJson(result).node("attributes.name").isAbsent();
    }

    @Test
    public void onIncludedAttributesInOtherResourceShouldNotContainFields() throws Exception {
        // GIVEN
        Task task = new Task();
        task.setName("some name");
        Project project = new Project();
        project.setId(1L);
        task.setProject(project);

        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
        QueryParams queryParams = queryParamsBuilder.buildQueryParams(
            Collections.singletonMap("fields[projects]", Collections.singleton("name")));
        JsonPath jsonPath = new PathBuilder(resourceRegistry).buildPath("/tasks");

        // WHEN
        String result = sut.writeValueAsString(new Container(task,
            new ResourceResponseContext(new JsonApiResponse(), jsonPath, queryParams)));

        // THEN
        assertThatJson(result).node("relationships.project").isAbsent();
        assertThatJson(result).node("attributes.name").isAbsent();
    }

    @Test
    public void onNestedAttributesShouldSerializeCorrectly() throws Exception {
        // GIVEN
        Pojo pojo = new Pojo();
        pojo.setOtherPojo(new OtherPojo()
            .setValue("some value"));

        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
        QueryParams queryParams = queryParamsBuilder.buildQueryParams(Collections.<String, Set<String>>emptyMap());
        JsonPath jsonPath = new PathBuilder(resourceRegistry).buildPath("/pojo");

        // WHEN
        String result = sut.writeValueAsString(new Container(pojo,
            new ResourceResponseContext(new JsonApiResponse(), jsonPath, queryParams)));

        // THEN
        assertThatJson(result).node("attributes.other-pojo.value").isEqualTo("some value");
    }

    @Test
    public void onMetaInformationShouldSerializeCorrectly() throws Exception {
        // GIVEN
        Task task = new Task();
        task.setMetaInformation(new MetaInformation() {
            public String name = "value";
        });

        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
        QueryParams queryParams = queryParamsBuilder.buildQueryParams(Collections.<String, Set<String>>emptyMap());
        JsonPath jsonPath = new PathBuilder(resourceRegistry).buildPath("/tasks");

        // WHEN
        String result = sut.writeValueAsString(new Container(task, new ResourceResponseContext(
            new JsonApiResponse(), jsonPath, queryParams)));

        // THEN
        assertThatJson(result).node("meta.name").isEqualTo("value");
    }

    @Test
    public void onLinksInformationShouldSerializeCorrectly() throws Exception {
        // GIVEN
        Task task = new Task();
        task.setLinksInformation(new LinksInformation() {
            public String name = "value";
        });

        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
        QueryParams queryParams = queryParamsBuilder.buildQueryParams(Collections.<String, Set<String>>emptyMap());
        JsonPath jsonPath = new PathBuilder(resourceRegistry).buildPath("/tasks");

        // WHEN
        String result = sut.writeValueAsString(new Container(task,
            new ResourceResponseContext(new JsonApiResponse(), jsonPath, queryParams)));

        // THEN
        assertThatJson(result).node("links.name").isEqualTo("value");
    }

    @Test
    public void onNoLinksInformationShouldSerializeWithDefaultLinks() throws Exception {
        // GIVEN
        Project project = new Project()
            .setId(1L);

        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
        QueryParams queryParams = queryParamsBuilder.buildQueryParams(Collections.<String, Set<String>>emptyMap());
        JsonPath jsonPath = new PathBuilder(resourceRegistry).buildPath("/projects");

        // WHEN
        String result = sut.writeValueAsString(new Container(project,
            new ResourceResponseContext(new JsonApiResponse(), jsonPath, queryParams)));

        // THEN
        assertThatJson(result).node("links.self").isEqualTo("https://service.local/projects/1");
    }
}

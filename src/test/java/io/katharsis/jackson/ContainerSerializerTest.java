package io.katharsis.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.queryParams.RequestParamsBuilder;
import io.katharsis.resource.RestrictedQueryParamsMembers;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.response.Container;
import org.junit.Test;

import java.util.Collections;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

public class ContainerSerializerTest extends BaseSerializerTest {

    @Test
    public void onSimpleObjectShouldIncludeType() throws Exception {
        // GIVEN
        Project project = new Project();

        // WHEN
        String result = sut.writeValueAsString(new Container(project, new RequestParams(null)));

        // THEN
        assertThatJson(result).node("type").isEqualTo("projects");
    }

    @Test
    public void onSimpleObjectShouldIncludeStringId() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setId(1L);

        // WHEN
        String result = sut.writeValueAsString(new Container(project, new RequestParams(null)));

        // THEN
        assertThatJson(result).node("id").isEqualTo("\"1\"");
    }

    @Test
    public void onSimpleObjectShouldIncludeAttributes() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setName("name");

        // WHEN
        String result = sut.writeValueAsString(new Container(project, new RequestParams(null)));

        // THEN
        assertThatJson(result).node("attributes.name").isEqualTo("name");
    }

    @Test
    public void onIncludedFieldsInParamsShouldContainIncludedList() throws Exception {
        // GIVEN
        Project project = new Project();
        project.setName("name");
        project.setDescription("description");

        RequestParamsBuilder requestParamsBuilder = new RequestParamsBuilder(new ObjectMapper());
        RequestParams requestParams = requestParamsBuilder.buildRequestParams(
                Collections.singletonMap(RestrictedQueryParamsMembers.fields.name(), "[\"name\"]"));

        // WHEN
        String result = sut.writeValueAsString(new Container(project, requestParams));

        // THEN
        assertThatJson(result).node("attributes.name").isEqualTo("name");
        assertThatJson(result).node("attributes.description").isAbsent();
    }
}

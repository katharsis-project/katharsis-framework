package io.katharsis.request.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestBodyTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void onPostDataWithSingleLinkageShouldMapToObject() throws Exception {
        // GIVEN
        String body = "{\"data\": {\"type\": \"tasks\", \"attributes\": {\"name\": \"asdasd\"}, \"relationships\": {\"project\": " +
                "{\"data\": {\"type\": \"projects\", \"id\": \"123\"}}}}}";

        // WHEN
        RequestBody result = objectMapper.readValue(body, RequestBody.class);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData()).isExactlyInstanceOf(DataBody.class);
        DataBody data = result.getSingleData();
        assertThat(data.getType()).isEqualTo("tasks");
        assertThat(data.getAttributes()).isNotNull();
        assertThat(data.getAttributes().getAttributes()).containsOnlyKeys("name");
        assertThat(data.getAttributes().getAttributes().get("name")).isEqualTo("asdasd");
        assertThat(data.getRelationships()).isNotNull();
        assertThat(data.getRelationships().getAdditionalProperties()).containsOnlyKeys("project");
        assertThat(data.getRelationships().getAdditionalProperties().get("project")).isInstanceOf(LinkageData.class);
        assertThat(((LinkageData) (data.getRelationships().getAdditionalProperties().get("project")))
                .getType()).isEqualTo("projects");
        assertThat(((LinkageData) (data.getRelationships().getAdditionalProperties().get("project")))
                .getId()).isEqualTo("123");
    }

    @Test
    public void onPostDataWithNullLinkageShouldMapToObject() throws Exception {
        // GIVEN
        String body = "{\"data\": {\"type\": \"tasks\", \"attributes\": {\"name\": \"asdasd\"}, \"relationships\": " +
                "{\"project\": {\"data\": null }}}}";

        // WHEN
        RequestBody result = objectMapper.readValue(body, RequestBody.class);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData()).isExactlyInstanceOf(DataBody.class);
        DataBody data = result.getSingleData();
        assertThat(data.getType()).isEqualTo("tasks");
        assertThat(data.getAttributes()).isNotNull();
        assertThat(data.getAttributes().getAttributes()).containsOnlyKeys("name");
        assertThat(data.getAttributes().getAttributes().get("name")).isEqualTo("asdasd");
        assertThat(data.getRelationships()).isNotNull();
        assertThat(data.getRelationships().getAdditionalProperties()).containsOnlyKeys("project");
        assertThat(data.getRelationships().getAdditionalProperties().get("project")).isNull();
    }

    @Test
    public void onPostDataWithListOfLinkageShouldMapToObject() throws Exception {
        // GIVEN
        String body = "{\"data\": {\"type\": \"tasks\", \"attributes\": {\"name\": \"asdasd\"}, \"relationships\": {\"project\":" +
                " {\"data\": [{\"type\": \"projects\", \"id\": \"123\"}]}}}}";

        // WHEN
        RequestBody result = objectMapper.readValue(body, RequestBody.class);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData()).isExactlyInstanceOf(DataBody.class);
        DataBody data = result.getSingleData();
        assertThat(data.getType()).isEqualTo("tasks");
        assertThat(data.getAttributes()).isNotNull();
        assertThat(data.getAttributes().getAttributes()).containsOnlyKeys("name");
        assertThat(data.getAttributes().getAttributes().get("name")).isEqualTo("asdasd");
        assertThat(data.getRelationships()).isNotNull();
        assertThat(data.getRelationships().getAdditionalProperties()).containsOnlyKeys("project");
        assertThat(data.getRelationships().getAdditionalProperties().get("project")).isInstanceOf(Iterable.class);
        assertThat(((Iterable<LinkageData>) (data.getRelationships().getAdditionalProperties().get("project"))))
                .hasSize(1);
        assertThat(((Iterable<LinkageData>) (data.getRelationships().getAdditionalProperties().get("project")))
                .iterator().next()).isInstanceOf(LinkageData.class);
        assertThat(((Iterable<LinkageData>) (data.getRelationships().getAdditionalProperties().get("project")))
                .iterator().next().getType()).isEqualTo("projects");
        assertThat(((Iterable<LinkageData>) (data.getRelationships().getAdditionalProperties().get("project")))
                .iterator().next().getId()).isEqualTo("123");
    }

    @Test
    public void onPostDataWithLifstOfLinkageShouldMapToObject() throws Exception {
        // GIVEN
        String body = "{\"data\": {\"type\": \"tasks\", \"attributes\": {\"name\": \"asdasd\"}, " +
                "\"relationships\": {\"project\": {\"data\": []}}}}";

        // WHEN
        RequestBody result = objectMapper.readValue(body, RequestBody.class);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData()).isExactlyInstanceOf(DataBody.class);
        DataBody data = result.getSingleData();
        assertThat(data.getType()).isEqualTo("tasks");
        assertThat(data.getAttributes()).isNotNull();
        assertThat(data.getAttributes().getAttributes()).containsOnlyKeys("name");
        assertThat(data.getAttributes().getAttributes().get("name")).isEqualTo("asdasd");
        assertThat(data.getRelationships()).isNotNull();
        assertThat(data.getRelationships().getAdditionalProperties()).containsOnlyKeys("project");
        assertThat(data.getRelationships().getAdditionalProperties().get("project")).isInstanceOf(Iterable.class);
        assertThat(((Iterable<LinkageData>) (data.getRelationships().getAdditionalProperties().get("project"))))
                .hasSize(0);
    }
}

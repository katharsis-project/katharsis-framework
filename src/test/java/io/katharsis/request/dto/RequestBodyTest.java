package io.katharsis.request.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestBodyTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void onPostDataWithSingleLinkageShouldMapToObject() throws Exception {
        // GIVEN
        String body = "{\"data\": {\"type\": \"tasks\", \"attributes\": {\"name\": \"asdasd\"}, \"links\": {\"project\": " +
                "{\"type\": \"projects\", \"id\": \"123\"}}}}";

        // WHEN
        RequestBody result = objectMapper.readValue(body, RequestBody.class);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getType()).isEqualTo("tasks");
        assertThat(result.getData().getAttributes()).isNotNull();
        assertThat(result.getData().getAttributes().getAttributes()).containsOnlyKeys("name");
        assertThat(result.getData().getAttributes().getAttributes().get("name")).isEqualTo("asdasd");
        assertThat(result.getData().getLinks()).isNotNull();
        assertThat(result.getData().getLinks().getAdditionalProperties()).containsOnlyKeys("project");
        assertThat(result.getData().getLinks().getAdditionalProperties().get("project")).isInstanceOf(Linkage.class);
        assertThat(((Linkage) (result.getData().getLinks().getAdditionalProperties().get("project")))
                .getType()).isEqualTo("projects");
        assertThat(((Linkage) (result.getData().getLinks().getAdditionalProperties().get("project")))
                .getId()).isEqualTo("123");
    }

    @Test
    public void onPostDataWithNullLinkageShouldMapToObject() throws Exception {
        // GIVEN
        String body = "{\"data\": {\"type\": \"tasks\", \"attributes\": {\"name\": \"asdasd\"}, \"links\": {\"project\": null}}}";

        // WHEN
        RequestBody result = objectMapper.readValue(body, RequestBody.class);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getType()).isEqualTo("tasks");
        assertThat(result.getData().getAttributes()).isNotNull();
        assertThat(result.getData().getAttributes().getAttributes()).containsOnlyKeys("name");
        assertThat(result.getData().getAttributes().getAttributes().get("name")).isEqualTo("asdasd");
        assertThat(result.getData().getLinks()).isNotNull();
        assertThat(result.getData().getLinks().getAdditionalProperties()).containsOnlyKeys("project");
        assertThat(result.getData().getLinks().getAdditionalProperties().get("project")).isNull();
    }

    @Test
    public void onPostDataWithListOfLinkageShouldMapToObject() throws Exception {
        // GIVEN
        String body = "{\"data\": {\"type\": \"tasks\", \"attributes\": {\"name\": \"asdasd\"}, \"links\": {\"project\":" +
                " [{\"type\": \"projects\", \"id\": \"123\"}]}}}";

        // WHEN
        RequestBody result = objectMapper.readValue(body, RequestBody.class);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getType()).isEqualTo("tasks");
        assertThat(result.getData().getAttributes()).isNotNull();
        assertThat(result.getData().getAttributes().getAttributes()).containsOnlyKeys("name");
        assertThat(result.getData().getAttributes().getAttributes().get("name")).isEqualTo("asdasd");
        assertThat(result.getData().getLinks()).isNotNull();
        assertThat(result.getData().getLinks().getAdditionalProperties()).containsOnlyKeys("project");
        assertThat(result.getData().getLinks().getAdditionalProperties().get("project")).isInstanceOf(Iterable.class);
        assertThat(((Iterable<Linkage>) (result.getData().getLinks().getAdditionalProperties().get("project"))))
                .hasSize(1);
        assertThat(((Iterable<Linkage>) (result.getData().getLinks().getAdditionalProperties().get("project")))
                .iterator().next()).isInstanceOf(Linkage.class);
        assertThat(((Iterable<Linkage>) (result.getData().getLinks().getAdditionalProperties().get("project")))
                .iterator().next().getType()).isEqualTo("projects");
        assertThat(((Iterable<Linkage>) (result.getData().getLinks().getAdditionalProperties().get("project")))
                .iterator().next().getId()).isEqualTo("123");
    }

    @Test
    public void onPostDataWithLifstOfLinkageShouldMapToObject() throws Exception {
        // GIVEN
        String body = "{\"data\": {\"type\": \"tasks\", \"attributes\": {\"name\": \"asdasd\"}, \"links\": {\"project\": []}}}";

        // WHEN
        RequestBody result = objectMapper.readValue(body, RequestBody.class);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getType()).isEqualTo("tasks");
        assertThat(result.getData().getAttributes()).isNotNull();
        assertThat(result.getData().getAttributes().getAttributes()).containsOnlyKeys("name");
        assertThat(result.getData().getAttributes().getAttributes().get("name")).isEqualTo("asdasd");
        assertThat(result.getData().getLinks()).isNotNull();
        assertThat(result.getData().getLinks().getAdditionalProperties()).containsOnlyKeys("project");
        assertThat(result.getData().getLinks().getAdditionalProperties().get("project")).isInstanceOf(Iterable.class);
        assertThat(((Iterable<Linkage>) (result.getData().getLinks().getAdditionalProperties().get("project"))))
                .hasSize(0);
    }
}

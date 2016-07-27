package io.katharsis.request.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestBodyTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        assertThat(getList(data.getAttributes().fieldNames())).containsOnly("name");
        assertThat(data.getAttributes().get("name").asText()).isEqualTo("asdasd");
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
        assertThat(getList(data.getAttributes().fieldNames())).containsOnly("name");
        assertThat(data.getAttributes().get("name").asText()).isEqualTo("asdasd");
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
        assertThat(getList(data.getAttributes().fieldNames())).containsOnly("name");
        assertThat(data.getAttributes().get("name").asText()).isEqualTo("asdasd");
        assertThat(data.getRelationships()).isNotNull();
        assertThat(data.getRelationships().getAdditionalProperties()).containsOnlyKeys("project");
        assertThat(data.getRelationships().getAdditionalProperties().get("project")).isInstanceOf(Iterable.class);
        //noinspection unchecked
        assertThat(((Iterable<LinkageData>) (data.getRelationships().getAdditionalProperties().get("project"))))
                .hasSize(1);
        //noinspection unchecked
        assertThat(((Iterable<LinkageData>) (data.getRelationships().getAdditionalProperties().get("project")))
                .iterator().next()).isInstanceOf(LinkageData.class);
        //noinspection unchecked
        assertThat(((Iterable<LinkageData>) (data.getRelationships().getAdditionalProperties().get("project")))
                .iterator().next().getType()).isEqualTo("projects");
        //noinspection unchecked
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
        assertThat(getList(data.getAttributes().fieldNames())).containsOnly("name");
        assertThat(data.getAttributes().get("name").asText()).isEqualTo("asdasd");
        assertThat(data.getRelationships()).isNotNull();
        assertThat(data.getRelationships().getAdditionalProperties()).containsOnlyKeys("project");
        assertThat(data.getRelationships().getAdditionalProperties().get("project")).isInstanceOf(Iterable.class);
        //noinspection unchecked
        assertThat(((Iterable<LinkageData>) (data.getRelationships().getAdditionalProperties().get("project"))))
                .hasSize(0);
    }

    private List<String> getList(Iterator<String> iter) {
        List<String> copy = new LinkedList<>();
        while (iter.hasNext())
            copy.add(iter.next());
        return copy;
    }
}

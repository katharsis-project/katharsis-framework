package io.katharsis.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;


public class DocumentSerializationTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void onPostDataWithSingleLinkageShouldMapToObject() throws Exception {
		// GIVEN
		String body = "{\"data\": {\"type\": \"tasks\", \"attributes\": {\"name\": \"asdasd\"}, \"relationships\": {\"project\": " + "{\"data\": {\"type\": \"projects\", \"id\": \"123\"}}}}}";

		// WHEN
		Document result = objectMapper.readValue(body, Document.class);

		// THEN
		assertThat(result).isNotNull();
		assertThat(result.getData()).isNotNull();
		assertThat(result.getData()).isExactlyInstanceOf(Resource.class);
		Resource data = result.getSingleData();
		assertThat(data.getType()).isEqualTo("tasks");
		assertThat(data.getAttributes()).isNotNull();
		assertThat(data.getAttributes().keySet()).containsOnly("name");
		assertThat(data.getAttributes().get("name").asText()).isEqualTo("asdasd");
		assertThat(data.getRelationships()).isNotNull();
		assertThat(data.getRelationships().keySet()).containsOnly("project");
		Relationship relationship = data.getRelationships().get("project");
		ResourceId relationshipData = (ResourceId) relationship.getData();
		assertThat(relationshipData.getType()).isEqualTo("projects");
		assertThat(relationshipData.getId()).isEqualTo("123");
	}

	@Test
	public void onPostDataWithNullLinkageShouldMapToObject() throws Exception {
		// GIVEN
		String body = "{\"data\": {\"type\": \"tasks\", \"attributes\": {\"name\": \"asdasd\"}, \"relationships\": " + "{\"project\": {\"data\": null }}}}";

		// WHEN
		Document result = objectMapper.readValue(body, Document.class);

		// THEN
		assertThat(result).isNotNull();
		assertThat(result.getData()).isNotNull();
		assertThat(result.getData()).isExactlyInstanceOf(Resource.class);
		Resource data = result.getSingleData();
		assertThat(data.getType()).isEqualTo("tasks");
		assertThat(data.getAttributes()).isNotNull();
		assertThat(data.getAttributes().keySet()).containsOnly("name");
		assertThat(data.getAttributes().get("name").asText()).isEqualTo("asdasd");
		assertThat(data.getRelationships()).isNotNull();
		assertThat(data.getRelationships().keySet()).containsOnly("project");
		assertThat(data.getRelationships().get("project")).isNull();
	}

	@Test
	public void onPostDataWithListOfLinkageShouldMapToObject() throws Exception {
		// GIVEN
		String body = "{\"data\": {\"type\": \"tasks\", \"attributes\": {\"name\": \"asdasd\"}, \"relationships\": {\"project\":" + " {\"data\": [{\"type\": \"projects\", \"id\": \"123\"}]}}}}";

		// WHEN
		Document result = objectMapper.readValue(body, Document.class);

		// THEN
		assertThat(result).isNotNull();
		assertThat(result.getData()).isNotNull();
		assertThat(result.getData()).isExactlyInstanceOf(Resource.class);
		Resource data = result.getSingleData();
		assertThat(data.getType()).isEqualTo("tasks");
		assertThat(data.getAttributes()).isNotNull();
		assertThat(data.getAttributes().keySet()).containsOnly("name");
		assertThat(data.getAttributes().get("name").asText()).isEqualTo("asdasd");
		assertThat(data.getRelationships()).isNotNull();
		assertThat(data.getRelationships().keySet()).containsOnly("project");
		assertThat(data.getRelationships().get("project")).isInstanceOf(Iterable.class);
		// noinspection unchecked
		assertThat(((Iterable<ResourceId>) (data.getRelationships().get("project").getData()))).hasSize(1);
		// noinspection unchecked
		assertThat(((Iterable<ResourceId>) (data.getRelationships().get("project")).getData()).iterator().next()).isInstanceOf(ResourceId.class);
		// noinspection unchecked
		assertThat(((Iterable<ResourceId>) (data.getRelationships().get("project")).getData()).iterator().next().getType()).isEqualTo("projects");
		// noinspection unchecked
		assertThat(((Iterable<ResourceId>) (data.getRelationships().get("project")).getData()).iterator().next().getId()).isEqualTo("123");
	}

	@Test
	public void onPostDataWithLifstOfLinkageShouldMapToObject() throws Exception {
		// GIVEN
		String body = "{\"data\": {\"type\": \"tasks\", \"attributes\": {\"name\": \"asdasd\"}, " + "\"relationships\": {\"project\": {\"data\": []}}}}";

		// WHEN
		Document result = objectMapper.readValue(body, Document.class);

		// THEN
		assertThat(result).isNotNull();
		assertThat(result.getData()).isNotNull();
		assertThat(result.getData()).isExactlyInstanceOf(Resource.class);
		Resource data = result.getSingleData();
		assertThat(data.getType()).isEqualTo("tasks");
		assertThat(data.getAttributes()).isNotNull();
		assertThat(data.getAttributes().keySet()).containsOnly("name");
		assertThat(data.getAttributes().get("name").asText()).isEqualTo("asdasd");
		assertThat(data.getRelationships()).isNotNull();
		assertThat(data.getRelationships().keySet()).containsOnly("project");
		assertThat(data.getRelationships().get("project")).isInstanceOf(Iterable.class);
		// noinspection unchecked
		assertThat(((Iterable<ResourceId>) (data.getRelationships().get("project")))).hasSize(0);
	}

	private List<String> getList(Iterator<String> iter) {
		List<String> copy = new LinkedList<>();
		while (iter.hasNext())
			copy.add(iter.next());
		return copy;
	}
}

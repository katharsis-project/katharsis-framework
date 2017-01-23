package io.katharsis.client.dynamic;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.client.AbstractClientTest;
import io.katharsis.client.KatharsisTestFeature;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.repository.ResourceRepositoryV2;
import io.katharsis.resource.Resource;
import io.katharsis.resource.list.ResourceList;

public class DynamicClientTest extends AbstractClientTest {

	@Before
	public void setup() {
		super.setup();
	}

	@Override
	protected TestApplication configure() {
		return new TestApplication(true);
	}

	protected void setupFeature(KatharsisTestFeature feature) {
		feature.addModule(new DynamicModule());
	}

	@Test
	public void test() throws JsonProcessingException, IOException {
		ResourceRepositoryV2<Resource, String> repository = client.getRepositoryForPath("dynamic");

		ObjectMapper mapper = new ObjectMapper();

		Resource resource = new Resource();
		resource.setId("john");
		resource.setType("dynamic");
		resource.getAttributes().put("value", mapper.readTree("\"doe\""));
		repository.create(resource);

		ResourceList<Resource> list = repository.findAll(new QuerySpec("dynamic"));
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("doe", list.get(0).getAttributes().get("value").asText());
	}

}

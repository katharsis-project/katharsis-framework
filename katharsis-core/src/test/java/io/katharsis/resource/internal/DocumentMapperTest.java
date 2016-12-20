package io.katharsis.resource.internal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.jackson.JsonApiModuleBuilder;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.queryspec.internal.QuerySpecAdapter;
import io.katharsis.resource.Document;
import io.katharsis.resource.Resource;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.resource.registry.ResourceRegistryBuilderTest;
import io.katharsis.resource.registry.ResourceRegistryTest;
import io.katharsis.response.JsonApiResponse;

public class DocumentMapperTest {

	private DocumentMapper mapper;
	private ResourceRegistry resourceRegistry;

	@Before
	public void setup() {
		ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(new ResourceFieldNameTransformer());
		ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(new SampleJsonServiceLocator(), resourceInformationBuilder);
		resourceRegistry = registryBuilder.build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, new ModuleRegistry(), new ConstantServiceUrlProvider(ResourceRegistryTest.TEST_MODELS_URL));

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JsonApiModuleBuilder().build(resourceRegistry, false));

		mapper = new DocumentMapper(resourceRegistry, objectMapper);
	}

	@Test
	public void test() {
		Task task = new Task();
		task.setId(2L);
		task.setName("sample task");
		JsonApiResponse response = new JsonApiResponse();
		response.setEntity(task);

		Document document = mapper.toDocument(response, toAdapter(new QuerySpec(Task.class)));
		Resource resource = document.getSingleData();
		Assert.assertEquals("2", resource.getId());
		Assert.assertEquals("tasks", resource.getType());
		Assert.assertEquals("sample task", resource.getAttributes().get("name").asText());
	}

	private QueryAdapter toAdapter(QuerySpec querySpec) {
		return new QuerySpecAdapter(querySpec, resourceRegistry);
	}

}

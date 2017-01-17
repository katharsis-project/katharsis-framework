package io.katharsis.resource.internal;

import org.junit.After;
import org.junit.Before;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.jackson.JsonApiModuleBuilder;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.queryspec.internal.QuerySpecAdapter;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.mock.repository.MockRepositoryUtil;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
import io.katharsis.resource.registry.ResourceRegistryBuilderTest;
import io.katharsis.resource.registry.ResourceRegistryTest;
import io.katharsis.response.JsonApiResponse;

public class AbstractDocumentMapperTest {

	protected DocumentMapper mapper;
	protected ResourceRegistry resourceRegistry;
	protected ObjectMapper objectMapper;

	@Before
	public void setup() {
		MockRepositoryUtil.clear();

		ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(new ResourceFieldNameTransformer());
		ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(new SampleJsonServiceLocator(), resourceInformationBuilder);
		resourceRegistry = registryBuilder.build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, new ModuleRegistry(), new ConstantServiceUrlProvider(ResourceRegistryTest.TEST_MODELS_URL));

		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JsonApiModuleBuilder().build(resourceRegistry, false));

		mapper = new DocumentMapper(resourceRegistry, objectMapper, null);
	}

	@After
	public void tearDown() {
		MockRepositoryUtil.clear();
	}

	protected QueryAdapter createAdapter() {
		return new QueryParamsAdapter(new QueryParams());
	}

	protected QueryAdapter toAdapter(QuerySpec querySpec) {
		return new QuerySpecAdapter(querySpec, resourceRegistry);
	}

	protected JsonApiResponse toResponse(Object entity) {
		JsonApiResponse response = new JsonApiResponse();
		response.setEntity(entity);
		return response;
	}

}

package io.katharsis.core.internal.dispatcher.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.core.internal.boot.EmptyPropertiesProvider;
import io.katharsis.core.internal.dispatcher.path.PathBuilder;
import io.katharsis.core.internal.jackson.JsonApiModuleBuilder;
import io.katharsis.core.internal.resource.AnnotationResourceInformationBuilder;
import io.katharsis.core.internal.resource.DocumentMapper;
import io.katharsis.legacy.locator.SampleJsonServiceLocator;
import io.katharsis.legacy.queryParams.DefaultQueryParamsParser;
import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.legacy.queryParams.QueryParamsBuilder;
import io.katharsis.legacy.registry.ResourceRegistryBuilder;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.resource.Resource;
import io.katharsis.resource.information.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.mock.repository.ProjectRepository;
import io.katharsis.resource.mock.repository.ProjectToTaskRepository;
import io.katharsis.resource.mock.repository.TaskRepository;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.resource.mock.repository.UserRepository;
import io.katharsis.resource.mock.repository.UserToProjectRepository;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilderTest;
import io.katharsis.resource.registry.ResourceRegistryTest;
import io.katharsis.utils.parser.TypeParser;

public abstract class BaseControllerTest {

	protected static final long TASK_ID = 1;
	protected static final long PROJECT_ID = 2;

	protected static final QueryParams REQUEST_PARAMS = new QueryParams();

	protected ObjectMapper objectMapper;
	protected PathBuilder pathBuilder;
	protected ResourceRegistry resourceRegistry;
	protected TypeParser typeParser;
	protected DocumentMapper documentMapper;
	protected QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void prepare() {
		ModuleRegistry moduleRegistry = new ModuleRegistry();
		ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(new ResourceFieldNameTransformer());
		ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(moduleRegistry, new SampleJsonServiceLocator(), resourceInformationBuilder);
		resourceRegistry = registryBuilder.build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, moduleRegistry, new ConstantServiceUrlProvider(ResourceRegistryTest.TEST_MODELS_URL));
		pathBuilder = new PathBuilder(resourceRegistry);
		typeParser = moduleRegistry.getTypeParser();
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JsonApiModuleBuilder().build(resourceRegistry, false));
		documentMapper = new DocumentMapper(resourceRegistry, objectMapper, new EmptyPropertiesProvider());
		UserRepository.clear();
		ProjectRepository.clear();
		TaskRepository.clear();
		UserToProjectRepository.clear();
		TaskToProjectRepository.clear();
		ProjectToTaskRepository.clear();
	}

	public Resource createTask() {
		Resource data = new Resource();
		data.setType("tasks");
		data.setId("1");

		try {
			data.setAttribute("name", objectMapper.readTree("\"sample task\""));
			data.setAttribute("data", objectMapper.readTree("\"asd\""));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return data;
	}

	public Resource createUser() {
		Resource data = new Resource();
		data.setType("users");
		data.setId("3");

		try {
			data.setAttribute("name", objectMapper.readTree("\"sample user\""));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return data;
	}

	public Resource createProject() {
		return createProject(Long.toString(PROJECT_ID));
	}

	public Resource createProject(String id) {
		Resource data = new Resource();
		data.setType("projects");
		data.setId(id);

		try {
			data.setAttribute("name", objectMapper.readTree("\"sample project\""));
			data.setAttribute("data", objectMapper.readTree("{\"data\" : \"asd\"}"));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return data;
	}

	protected void addParams(Map<String, Set<String>> params, String key, String value) {
		params.put(key, new HashSet<>(Arrays.asList(value)));
	}
}

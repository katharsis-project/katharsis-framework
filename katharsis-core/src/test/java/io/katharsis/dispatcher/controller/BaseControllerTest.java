package io.katharsis.dispatcher.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.katharsis.internal.boot.EmptyPropertiesProvider;
import io.katharsis.jackson.JsonApiModuleBuilder;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.request.path.PathBuilder;
import io.katharsis.resource.Resource;
import io.katharsis.resource.field.ResourceFieldNameTransformer;
import io.katharsis.resource.include.IncludeLookupSetter;
import io.katharsis.resource.information.AnnotationResourceInformationBuilder;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.mock.repository.ProjectRepository;
import io.katharsis.resource.mock.repository.ProjectToTaskRepository;
import io.katharsis.resource.mock.repository.TaskRepository;
import io.katharsis.resource.mock.repository.TaskToProjectRepository;
import io.katharsis.resource.mock.repository.UserRepository;
import io.katharsis.resource.mock.repository.UserToProjectRepository;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilder;
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
	protected IncludeLookupSetter includeFieldSetter;
	protected QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void prepare() {
		ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(new ResourceFieldNameTransformer());
		ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(new SampleJsonServiceLocator(), resourceInformationBuilder);
		resourceRegistry = registryBuilder.build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, new ModuleRegistry(), new ConstantServiceUrlProvider(ResourceRegistryTest.TEST_MODELS_URL));
		pathBuilder = new PathBuilder(resourceRegistry);
		typeParser = new TypeParser();
		includeFieldSetter = new IncludeLookupSetter(resourceRegistry, new EmptyPropertiesProvider());
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JsonApiModuleBuilder().build(resourceRegistry, false));
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
			data.setAttribute("data", objectMapper.readTree("\"asd\""));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return data;
	}

	protected void addParams(Map<String, Set<String>> params, String key, String value) {
		params.put(key, new HashSet<>(Arrays.asList(value)));
	}
}

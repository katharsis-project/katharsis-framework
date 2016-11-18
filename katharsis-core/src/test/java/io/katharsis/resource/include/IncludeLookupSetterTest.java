package io.katharsis.resource.include;

import io.katharsis.internal.boot.EmptyPropertiesProvider;
import io.katharsis.internal.boot.KatharsisBootProperties;
import io.katharsis.internal.boot.PropertiesProvider;
import io.katharsis.queryParams.DefaultQueryParamsParser;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.queryParams.QueryParamsBuilder;
import io.katharsis.queryspec.internal.QueryAdapter;
import io.katharsis.queryspec.internal.QueryParamsAdapter;
import io.katharsis.resource.mock.models.Project;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.mock.repository.MockRepositoryUtil;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.repository.adapter.RelationshipRepositoryAdapter;
import io.katharsis.resource.registry.repository.adapter.ResourceRepositoryAdapter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class IncludeLookupSetterTest {

    protected ResourceRegistry resourceRegistry;
    private IncludeLookupSetter sut;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Before
    public void setUp() throws Exception {
        MockRepositoryUtil.clear();

        // setup repositories
        resourceRegistry = MockRepositoryUtil.setupResourceRegistry();

        // get repositories
        ResourceRepositoryAdapter taskRepository = resourceRegistry.getEntry(Task.class).getResourceRepository(null);
        RelationshipRepositoryAdapter relRepositoryTaskToProject = resourceRegistry.getEntry(Task.class)
                .getRelationshipRepositoryForClass(Project.class, null);
        RelationshipRepositoryAdapter relRepositoryProjectToTask = resourceRegistry.getEntry(Project.class)
                .getRelationshipRepositoryForClass(Task.class, null);
        ResourceRepositoryAdapter projectRepository = resourceRegistry.getEntry(Project.class).getResourceRepository(null);

        // setup test data
        Project project = new Project();
        project.setId(2L);
        projectRepository.create(project, null);
        Task task = new Task();
        task.setId(1L);
        taskRepository.create(task, null);
        relRepositoryTaskToProject.setRelation(task, project.getId(), "includedProject", null);
        relRepositoryTaskToProject.setRelation(task, project.getId(), "project", null);
        relRepositoryTaskToProject.addRelations(task, Collections.singletonList(project.getId()), "includedProjects", null);


        // setup deep nested relationship
        Task includedTask = new Task();
        includedTask.setId(3L);
        taskRepository.create(includedTask, null);
        relRepositoryProjectToTask.setRelation(project, includedTask.getId(), "includedTask", null);
        Project deepIncludedProject = new Project();
        deepIncludedProject.setId(2L);
        projectRepository.create(project, null);
        relRepositoryTaskToProject.setRelation(includedTask, deepIncludedProject.getId(), "includedProject", null);
        relRepositoryTaskToProject.addRelations(includedTask, Collections.singletonList(project.getId()), "includedProjects", null);


        sut = new IncludeLookupSetter(resourceRegistry, new EmptyPropertiesProvider());
    }

    @After
    public void tearDown() {
        MockRepositoryUtil.clear();
    }

    protected QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());

    private void addParams(Map<String, Set<String>> params, String key, String value) {
        params.put(key, new HashSet<>(Arrays.asList(value)));
    }

    @Test
    public void includeOneRelationLookup() throws Exception {
        Map<String, Set<String>> params = new HashMap<String, Set<String>>();
        addParams(params, "include[tasks]", "includedProject");
        QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
        QueryAdapter queryAdapter = new QueryParamsAdapter(queryParams);

        Task task = new Task();
        task.setId(1L);
        sut.setIncludedElements("tasks", task, queryAdapter, null);
        Assert.assertNotNull(task.getIncludedProject());
    }

    @Test
    public void includeManyRelationLookup() throws Exception {
        Map<String, Set<String>> params = new HashMap<String, Set<String>>();
        addParams(params, "include[tasks]", "includedProjects");
        QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
        QueryAdapter queryAdapter = new QueryParamsAdapter(queryParams);

        Task task = new Task();
        task.setId(1L);

        sut.setIncludedElements("tasks", task, queryAdapter, null);
        Assert.assertNotNull(task.getIncludedProjects());
        Assert.assertEquals(1, task.getIncludedProjects().size());
    }

    @Test
    public void includeOneDeepNestedRelationLookup() throws Exception {
        Map<String, Set<String>> params = new HashMap<String, Set<String>>();
        addParams(params, "include[tasks]", "includedProject.includedTask.includedProject");
        QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
        QueryAdapter queryAdapter = new QueryParamsAdapter(queryParams);

        Task task = new Task();
        task.setId(3L);
        sut.setIncludedElements("tasks", task, queryAdapter, null);
        Assert.assertNotNull(task.getIncludedProject());
        Assert.assertNotNull(task.getIncludedProject().getIncludedTask());
        Assert.assertNotNull(task.getIncludedProject().getIncludedTask().getIncludedProject());
    }

    @Test
    public void includeManyDeepNestedRelationLookup() throws Exception {
        Map<String, Set<String>> params = new HashMap<String, Set<String>>();
        addParams(params, "include[tasks]", "includedProjects.includedTask.includedProject");

        QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
        QueryAdapter queryAdapter = new QueryParamsAdapter(queryParams);

        Task task = new Task();
        task.setId(3L);
        sut.setIncludedElements("tasks", task, queryAdapter, null);
        Assert.assertNotNull(task.getIncludedProjects());
        Assert.assertNotNull(task.getIncludedProjects().get(0).getIncludedTask());
        Assert.assertNotNull(task.getIncludedProjects().get(0).getIncludedTask().getIncludedProject());
    }

    @Test
    public void testNullPropertiesProviderResponse() throws Exception {
        sut = new IncludeLookupSetter(resourceRegistry, new PropertiesProvider() {
            @Override
            public String getProperty(String key) {
                return null;
            }
        });
    }

    @Test
    public void includePropertiesProviderAllTrueRelationshipLookup() throws Exception {
        sut = new IncludeLookupSetter(resourceRegistry, new PropertiesProvider() {
            @Override
            public String getProperty(String key) {
                return "true";
            }
        });

        Map<String, Set<String>> params = new HashMap<String, Set<String>>();
        addParams(params, "include[tasks]", "project");

        QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
        QueryAdapter queryAdapter = new QueryParamsAdapter(queryParams);

        Task task = new Task();
        task.setId(1L);
        sut.setIncludedElements("tasks", task, queryAdapter, null);
        Assert.assertNotNull(task.getProject());
    }

    @Test
    public void includePropertiesProviderNonOverwriteRelationshipLookup() throws Exception {
        sut = new IncludeLookupSetter(resourceRegistry, new PropertiesProvider() {
            @Override
            public String getProperty(String key) {
                if (key.equalsIgnoreCase(KatharsisBootProperties.INCLUDE_AUTOMATICALLY_OVERWRITE)) {
                    return "false";
                }
                return "true";
            }
        });

        Map<String, Set<String>> params = new HashMap<String, Set<String>>();
        addParams(params, "include[tasks]", "project");

        QueryParams queryParams = queryParamsBuilder.buildQueryParams(params);
        QueryAdapter queryAdapter = new QueryParamsAdapter(queryParams);
        Project project = new Project();
        Task task = new Task();
        task.setId(1L);
        task.setProject(project);
        sut.setIncludedElements("tasks", task, queryAdapter, null);
        Assert.assertNotNull(task.getProject());
        Assert.assertTrue(task.getProject() == project);
    }
}

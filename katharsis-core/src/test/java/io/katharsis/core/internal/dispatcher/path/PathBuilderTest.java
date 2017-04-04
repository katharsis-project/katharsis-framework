package io.katharsis.core.internal.dispatcher.path;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import io.katharsis.core.internal.dispatcher.path.ActionPath;
import io.katharsis.core.internal.dispatcher.path.FieldPath;
import io.katharsis.core.internal.dispatcher.path.JsonPath;
import io.katharsis.core.internal.dispatcher.path.PathBuilder;
import io.katharsis.core.internal.dispatcher.path.PathIds;
import io.katharsis.core.internal.dispatcher.path.RelationshipsPath;
import io.katharsis.core.internal.dispatcher.path.ResourcePath;
import io.katharsis.core.internal.resource.AnnotationResourceInformationBuilder;
import io.katharsis.errorhandling.exception.RepositoryNotFoundException;
import io.katharsis.errorhandling.exception.ResourceException;
import io.katharsis.errorhandling.exception.ResourceFieldNotFoundException;
import io.katharsis.legacy.locator.SampleJsonServiceLocator;
import io.katharsis.legacy.registry.ResourceRegistryBuilder;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.repository.information.RepositoryAction;
import io.katharsis.repository.information.ResourceRepositoryInformation;
import io.katharsis.resource.information.ResourceFieldNameTransformer;
import io.katharsis.resource.information.ResourceInformationBuilder;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.resource.registry.ConstantServiceUrlProvider;
import io.katharsis.resource.registry.RegistryEntry;
import io.katharsis.resource.registry.ResourceRegistry;
import io.katharsis.resource.registry.ResourceRegistryBuilderTest;
import io.katharsis.resource.registry.ResourceRegistryTest;

public class PathBuilderTest {

    private PathBuilder pathBuilder;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void prepare() {
    	ModuleRegistry moduleRegistry = new ModuleRegistry();
    	
        ResourceInformationBuilder resourceInformationBuilder = new AnnotationResourceInformationBuilder(
            new ResourceFieldNameTransformer());
        ResourceRegistryBuilder registryBuilder = new ResourceRegistryBuilder(moduleRegistry, new SampleJsonServiceLocator(),
            resourceInformationBuilder);
        ResourceRegistry resourceRegistry = registryBuilder
            .build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, moduleRegistry, new ConstantServiceUrlProvider(ResourceRegistryTest.TEST_MODELS_URL));

        pathBuilder = new PathBuilder(resourceRegistry);
        
        RegistryEntry entry = resourceRegistry.findEntry(Task.class);
        ResourceRepositoryInformation repositoryInformation = entry.getRepositoryInformation();
        repositoryInformation.getActions().put("someRepositoryAction", Mockito.mock(RepositoryAction.class));
        repositoryInformation.getActions().put("someResourceAction", Mockito.mock(RepositoryAction.class));
    }

    @Test
    public void onEmptyPathShouldThrowResourceException() {
        // GIVEN
        String path = "/";

        // THEN
        expectedException.expect(ResourceException.class);

        // WHEN
        pathBuilder.buildPath(path);
    }

    @Test
    public void onFlatResourcePathShouldReturnFlatPath() {
        // GIVEN
        String path = "/tasks/";

        // WHEN
        JsonPath jsonPath = pathBuilder.buildPath(path);

        // THEN
        assertThat(jsonPath).isEqualTo(new ResourcePath("tasks"));
        assertThat(jsonPath.isCollection()).isTrue();
    }

    @Test
    public void onFlatResourceInstancePathShouldReturnFlatPath() {
        // GIVEN
        String path = "/tasks/1";

        // WHEN
        JsonPath jsonPath = pathBuilder.buildPath(path);

        // THEN
        assertThat(jsonPath).isEqualTo(new ResourcePath("tasks", new PathIds("1")));
        assertThat(jsonPath.isCollection()).isFalse();
    }
    
    @Test
    public void onRepositoryActionShouldActionPath() {
        // GIVEN
        String path = "/tasks/someRepositoryAction";

        // WHEN
        JsonPath jsonPath = pathBuilder.buildPath(path);

        // THEN
        JsonPath expectedPath = new ActionPath("someRepositoryAction");
        expectedPath.setParentResource(new ResourcePath("tasks"));
        assertThat(jsonPath).isEqualTo(expectedPath);
    }
    
    @Test
    public void onResourceActionShouldActionPath() {
        // GIVEN
        String path = "/tasks/123/someResourceAction";

        // WHEN
        JsonPath jsonPath = pathBuilder.buildPath(path);

        // THEN
        JsonPath expectedPath = new ActionPath("someResourceAction");
        expectedPath.setParentResource(new ResourcePath("tasks", new PathIds("123")));
        assertThat(jsonPath).isEqualTo(expectedPath);
    }
    

    @Test
    public void onNestedResourcePathShouldReturnNestedPath() {
        // GIVEN
        String path = "/tasks/1/project";

        // WHEN
        JsonPath jsonPath = pathBuilder.buildPath(path);

        // THEN
        JsonPath expectedPath = new FieldPath("project");
        expectedPath.setParentResource(new ResourcePath("tasks", new PathIds("1")));
        assertThat(jsonPath).isEqualTo(expectedPath);
    }

    @Test
    public void onNestedResourceInstancePathShouldThrowException() {
        // GIVEN
        String path = "/tasks/1/project/2";

        // THEN
        expectedException.expect(ResourceException.class);
        expectedException.expectMessage("RelationshipsPath and FieldPath cannot contain ids");

        // WHEN
        pathBuilder.buildPath(path);
    }

    @Test
    public void onNestedResourceRelationshipPathShouldReturnNestedPath() {
        // GIVEN
        String path = "/tasks/1/relationships/project/";

        // WHEN
        JsonPath jsonPath = pathBuilder.buildPath(path);

        // THEN
        JsonPath expectedPath = new RelationshipsPath("project");
        expectedPath.setParentResource(new ResourcePath("tasks", new PathIds("1")));

        assertThat(jsonPath).isEqualTo(expectedPath);
    }

    @Test
    public void onNonRelationshipFieldShouldThrowException() {
        // GIVEN
        String path = "/tasks/1/relationships/name/";

        // THEN
        expectedException.expect(ResourceFieldNotFoundException.class);
        expectedException.expectMessage("name");

        // WHEN
        pathBuilder.buildPath(path);
    }

    @Test
    public void onRelationshipFieldInRelationshipsShouldThrowException() {
        // GIVEN
        String path = "/users/1/relationships/projects";

        // THEN
        expectedException.expect(ResourceFieldNotFoundException.class);
        expectedException.expectMessage("projects");

        // WHEN
        pathBuilder.buildPath(path);
    }

    @Test
    public void onNestedWrongResourceRelationshipPathShouldThrowException() {
        // GIVEN
        String path = "/tasks/1/relationships/";

        // THEN
        expectedException.expect(ResourceFieldNotFoundException.class);

        // WHEN
        pathBuilder.buildPath(path);
    }

    @Test
    public void onRelationshipsPathWithIdShouldThrowException() {
        // GIVEN
        String path = "/tasks/1/relationships/project/1";

        // THEN
        expectedException.expect(ResourceException.class);
        expectedException.expectMessage("RelationshipsPath and FieldPath cannot contain ids");

        // WHEN
        pathBuilder.buildPath(path);
    }

    @Test
    public void onNonExistingFieldShouldThrowException() {
        // GIVEN
        String path = "/tasks/1/nonExistingField/";

        // THEN
        expectedException.expect(ResourceFieldNotFoundException.class);
        expectedException.expectMessage("nonExistingField");

        // WHEN
        pathBuilder.buildPath(path);
    }

    @Test
    public void onNonExistingResourceShouldThrowException() {
        // GIVEN
        String path = "/nonExistingResource";

        // THEN
        expectedException.expect(RepositoryNotFoundException.class);

        // WHEN
        pathBuilder.buildPath(path);
    }

    @Test
    public void onResourceStaringWithRelationshipsShouldThrowException() {
        // GIVEN
        String path = "/relationships";

        // THEN
        expectedException.expect(RepositoryNotFoundException.class);

        // WHEN
        pathBuilder.buildPath(path);
    }

    @Test
    public void onMultipleResourceInstancesPathShouldReturnCollectionPath() {
        // GIVEN
        String path = "/tasks/1,2";

        // WHEN
        JsonPath jsonPath = pathBuilder.buildPath(path);

        // THEN
        Assert.assertTrue(jsonPath.isCollection());
        Assert.assertEquals(jsonPath.getIds().getIds(), Arrays.asList("1", "2"));
    }

    @Test
    public void onUrlEncodedMultipleResourceInstancesPathShouldReturnCollectionPath() {
        // GIVEN
        String path = "/tasks/1%2C2";

        // WHEN
        JsonPath jsonPath = pathBuilder.buildPath(path);

        // THEN
        Assert.assertTrue(jsonPath.isCollection());
        Assert.assertEquals(jsonPath.getIds().getIds(), Arrays.asList("1", "2"));
    }

    @Test
    public void onSimpleResourcePathShouldReturnCorrectStringPath() {
        // GIVEN
        JsonPath jsonPath = new ResourcePath("tasks");

        // WHEN
        String result = PathBuilder.buildPath(jsonPath);

        // THEN
        assertThat(result).isEqualTo("/tasks/");
    }

    @Test
    public void onResourcePathWithIdsShouldReturnCorrectStringPath() {
        // GIVEN
        JsonPath jsonPath = new ResourcePath("tasks", new PathIds(Arrays.asList("1", "2")));

        // WHEN
        String result = PathBuilder.buildPath(jsonPath);

        // THEN
        assertThat(result).isEqualTo("/tasks/1,2/");
    }

    @Test
    public void onResourcePathWithIdsAndRelationshipsPathShouldReturnCorrectStringPath() {
        // GIVEN
        JsonPath parentJsonPath = new ResourcePath("tasks", new PathIds(Collections.singletonList("1")));
        JsonPath jsonPath = new RelationshipsPath("project");
        jsonPath.setParentResource(parentJsonPath);

        // WHEN
        String result = PathBuilder.buildPath(jsonPath);

        // THEN
        assertThat(result).isEqualTo("/tasks/1/relationships/project/");
    }

    @Test
    public void onResourcePathWithIdsAndFieldPathShouldReturnCorrectStringPath() {
        // GIVEN
        JsonPath parentJsonPath = new ResourcePath("tasks", new PathIds(Collections.singletonList("1")));
        JsonPath jsonPath = new FieldPath("project");
        jsonPath.setParentResource(parentJsonPath);

        // WHEN
        String result = PathBuilder.buildPath(jsonPath);

        // THEN
        assertThat(result).isEqualTo("/tasks/1/project/");
    }

    @Test
    public void onFieldNameAsSameAsResourceShouldBuildCorrectPath() {
        // GIVEN
        String path = "/tasks/1/projects";

        // WHEN
        JsonPath jsonPath = pathBuilder.buildPath(path);

        // THEN
        JsonPath expectedPath = new FieldPath("projects");
        expectedPath.setParentResource(new ResourcePath("tasks", new PathIds("1")));
        assertThat(jsonPath).isEqualTo(expectedPath);
    }
}

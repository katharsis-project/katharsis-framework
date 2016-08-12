package io.katharsis.resource.registry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.exception.init.ResourceNotFoundInitializationException;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.utils.java.Optional;

public class ResourceRegistryTest {

    public static final String TEST_MODELS_URL = "https://service.local";
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private ResourceRegistry resourceRegistry;

    @Before
    public void resetResourceRegistry() {
        resourceRegistry = new ResourceRegistry(TEST_MODELS_URL);
    }

    @Test
    public void onExistingTypeShouldReturnEntry() {
    	resourceRegistry.addEntry(Task.class, new RegistryEntry(new ResourceInformation(Task.class, "tasks", null, null, null), null, null));
        RegistryEntry tasksEntry = resourceRegistry.getEntry("tasks");
        assertThat(tasksEntry).isNotNull();
    }

    @Test
    public void onExistingClassShouldReturnEntry() {
    	resourceRegistry.addEntry(Task.class, new RegistryEntry(new ResourceInformation(Task.class, "tasks", null, null, null), null, null));
        RegistryEntry tasksEntry = resourceRegistry.getEntry(Task.class);
        assertThat(tasksEntry).isNotNull();
    }

    @Test
    public void onExistingTypeShouldReturnUrl() {
    	resourceRegistry.addEntry(Task.class, new RegistryEntry(new ResourceInformation(Task.class, "tasks", null, null, null), null, null));
        String resourceUrl = resourceRegistry.getResourceUrl(Task.class);
        assertThat(resourceUrl).isEqualTo(TEST_MODELS_URL + "/tasks");
    }

    @Test
    public void onNonExistingTypeShouldReturnNull() {
        RegistryEntry entry = resourceRegistry.getEntry("nonExistingType");
        assertThat(entry).isNull();
    }

    @Test
    public void onNonExistingClassShouldThrowException() {
        expectedException.expect(ResourceNotFoundInitializationException.class);
        resourceRegistry.getEntry(Long.class);
    }

    @Test
    public void onNonExistingClassShouldReturnNull() {
        String result = resourceRegistry.getResourceType(Long.class);
        assertThat(result).isNull();
    }
    

    public static class Task$Proxy extends Task{
    }
    
    @Test
    public void onResourceClassReturnCorrectClass() {
    	resourceRegistry.addEntry(Task.class, new RegistryEntry(null, null, null));
    	
        // WHEN
        Class<?> clazz = resourceRegistry.getResourceClass(Task$Proxy.class).get();

        // THEN
        assertThat(clazz).isNotNull();
        assertThat(clazz).hasAnnotation(JsonApiResource.class);
        assertThat(clazz).isEqualTo(Task.class);
    }

    @Test
    public void onResourceClassReturnCorrectParentInstanceClass() {
    	resourceRegistry.addEntry(Task.class, new RegistryEntry(null, null, null));
        Task$Proxy resource = new Task$Proxy();

        // WHEN
        Class<?> clazz = resourceRegistry.getResourceClass(resource).get();

        // THEN
        assertThat(clazz).isEqualTo(Task.class);
    }
    
    @Test
    public void onResourceClassReturnCorrectInstanceClass() {
    	resourceRegistry.addEntry(Task.class, new RegistryEntry(null, null, null));
        Task resource = new Task();

        // WHEN
        Class<?> clazz = resourceRegistry.getResourceClass(resource).get();

        // THEN
        assertThat(clazz).isEqualTo(Task.class);
    }

    @Test
    public void onResourceClassReturnNoInstanceClass() {
    	resourceRegistry.addEntry(Task.class, new RegistryEntry(null, null, null));
    	Object resource = new Object();

        // WHEN
        Optional<Class<?>> clazz = resourceRegistry.getResourceClass(resource);

        // THEN
        assertThat(clazz.isPresent()).isFalse();
    }

}

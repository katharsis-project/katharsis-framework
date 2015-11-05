package io.katharsis.resource.registry;

import io.katharsis.resource.exception.init.ResourceNotFoundInitializationException;
import io.katharsis.resource.mock.models.Task;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.*;

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
        resourceRegistry.addEntry(Task.class, new RegistryEntry<>(null, null, null));
        RegistryEntry tasksEntry = resourceRegistry.getEntry("tasks");
        assertThat(tasksEntry).isNotNull();
    }

    @Test
    public void onExistingClassShouldReturnEntry() {
        resourceRegistry.addEntry(Task.class, new RegistryEntry<>(null, null, null));
        RegistryEntry tasksEntry = resourceRegistry.getEntry(Task.class);
        assertThat(tasksEntry).isNotNull();
    }

    @Test
    public void onExistingTypeShouldReturnUrl() {
        resourceRegistry.addEntry(Task.class, new RegistryEntry<>(null, null, null));
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
}

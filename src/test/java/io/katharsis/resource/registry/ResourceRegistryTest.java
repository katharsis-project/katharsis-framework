package io.katharsis.resource.registry;

import io.katharsis.resource.exception.init.ResourceNotFoundInitalizationException;
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
    private ResourceRegistry resourceRegisty;

    @Before
    public void resetResourceRegistry() {
        resourceRegisty = new ResourceRegistry(TEST_MODELS_URL);
    }

    @Test
    public void onExistingTypeShouldReturnEntry() {
        resourceRegisty.addEntry(Task.class, new RegistryEntry<>(null, null));
        RegistryEntry tasksEntry = resourceRegisty.getEntry("tasks");
        assertThat(tasksEntry).isNotNull();
    }

    @Test
    public void onExistingClassShouldReturnEntry() {
        resourceRegisty.addEntry(Task.class, new RegistryEntry<>(null, null));
        RegistryEntry tasksEntry = resourceRegisty.getEntry(Task.class);
        assertThat(tasksEntry).isNotNull();
    }

    @Test
    public void onExistingTypeShouldReturnUrl() {
        resourceRegisty.addEntry(Task.class, new RegistryEntry<>(null, null));
        String resourceUrl = resourceRegisty.getResourceUrl(Task.class);
        assertThat(resourceUrl).isEqualTo(TEST_MODELS_URL + "/tasks");
    }

    @Test
    public void onNonExistingTypeShouldReturnNull() {
        RegistryEntry entry = resourceRegisty.getEntry("nonExistingType");
        assertThat(entry).isNull();
    }

    @Test
    public void onNonExistingClassShouldThrowException() {
        expectedException.expect(ResourceNotFoundInitalizationException.class);
        resourceRegisty.getEntry(Long.class);
    }
}

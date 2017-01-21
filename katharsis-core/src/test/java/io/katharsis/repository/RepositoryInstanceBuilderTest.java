package io.katharsis.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import io.katharsis.errorhandling.exception.RepositoryInstanceNotFoundException;
import io.katharsis.legacy.locator.JsonServiceLocator;
import io.katharsis.legacy.locator.SampleJsonServiceLocator;
import io.katharsis.legacy.registry.RepositoryInstanceBuilder;
import io.katharsis.resource.mock.repository.TaskRepository;

public class RepositoryInstanceBuilderTest {

    @Test
    public void onExistingInstanceShouldReturnValue() throws Exception {
        // GIVEN
        RepositoryInstanceBuilder<TaskRepository> sut =
            new RepositoryInstanceBuilder<>(new SampleJsonServiceLocator(), TaskRepository.class);

        // WHEN
        TaskRepository result = sut.buildRepository();

        // THEN
        assertThat(sut.getRepositoryClass()).isEqualTo(TaskRepository.class);
        assertThat(result).isInstanceOf(TaskRepository.class);
    }

    @Test(expected = RepositoryInstanceNotFoundException.class)
    public void onNullInstanceShouldThrowException() throws Exception {
        // GIVEN
        RepositoryInstanceBuilder<TaskRepository> sut =
            new RepositoryInstanceBuilder<>(new JsonServiceLocator() {
                @Override
                public <T> T getInstance(Class<T> clazz) {
                    return null;
                }
            }, TaskRepository.class);

        // WHEN
        sut.buildRepository();
    }
}

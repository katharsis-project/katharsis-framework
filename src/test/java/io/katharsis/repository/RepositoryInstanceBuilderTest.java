package io.katharsis.repository;

import io.katharsis.locator.JsonServiceLocator;
import io.katharsis.locator.SampleJsonServiceLocator;
import io.katharsis.repository.exception.RepositoryInstanceNotFoundException;
import io.katharsis.resource.mock.repository.TaskRepository;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

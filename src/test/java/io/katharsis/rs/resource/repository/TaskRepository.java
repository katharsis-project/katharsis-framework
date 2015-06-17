package io.katharsis.rs.resource.repository;

import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.rs.resource.exception.ExampleException;
import io.katharsis.rs.resource.model.Task;

import java.util.Arrays;

public class TaskRepository implements ResourceRepository<Task, Long> {
    @Override
    public <S extends Task> S save(S entity) {
        return null;
    }

    @Override
    public Task findOne(Long aLong, RequestParams requestParams) {
        // Simulates error and throws an Exception to test exception handling.
        if (aLong == 5) {
            throw new ExampleException(ExampleException.ERROR_ID, ExampleException.ERROR_TITLE);
        }
        Task task = new Task(aLong, "Some task");
        return task;
    }

    @Override
    public Iterable<Task> findAll(RequestParams requestParams) {
        return findAll(null, requestParams);
    }

    @Override
    public Iterable<Task> findAll(Iterable<Long> taskIds, RequestParams requestParams) {
        return Arrays.asList(new Task(1L, "First task"));
    }

    @Override
    public void delete(Long aLong) {

    }
}

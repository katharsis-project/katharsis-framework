package io.katharsis.rs.resource.repository;

import io.katharsis.legacy.repository.annotations.*;
import io.katharsis.rs.resource.exception.ExampleException;
import io.katharsis.rs.resource.model.Task;

import javax.ws.rs.HeaderParam;
import java.util.Collections;

@JsonApiResourceRepository(Task.class)
public class TaskRepository {

    @JsonApiSave
    public <S extends Task> S save(S entity) {
        return null;
    }

    @JsonApiFindOne
    public Task findOne(Long aLong, @HeaderParam("x-test") String header) {
        // Simulates error and throws an Exception to test exception handling.
        if (aLong == 5) {
            throw new ExampleException(ExampleException.ERROR_ID, ExampleException.ERROR_TITLE);
        }
        Task task = new Task(aLong, header);
        return task;
    }

    @JsonApiFindAll
    public Iterable<Task> findAll() {
        return findAll(null);
    }

    @JsonApiFindAllWithIds
    public Iterable<Task> findAll(Iterable<Long> ids) {
        return Collections.singletonList(new Task(1L, "First task"));
    }

    @JsonApiDelete
    public void delete(Long aLong) {

    }
}

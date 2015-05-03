package io.katharsis.rs.resource.repository;

import io.katharsis.repository.ResourceRepository;
import io.katharsis.rs.resource.model.Task;

import java.util.Arrays;

public class TaskRepository implements ResourceRepository<Task, Long> {
    @Override
    public <S extends Task> S save(S entity) {
        return null;
    }

    @Override
    public <S extends Task> S update(S s) {
        return null;
    }

    @Override
    public Task findOne(Long aLong) {
        Task task = new Task(aLong, "Some task");
        return task;
    }

    @Override
    public Iterable<Task> findAll() {
        return Arrays.asList(new Task(1L, "First task"));
    }

    @Override
    public void delete(Long aLong) {

    }
}

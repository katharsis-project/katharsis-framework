package io.katharsis.resource.mock.repository;

import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.mock.models.Task;

import java.util.LinkedList;

public class TaskRepository implements ResourceRepository<Task, Long> {
    @Override
    public <S extends Task> S save(S entity) {
        return null;
    }

    @Override
    public Task findOne(Long aLong) {
        return null;
    }

    @Override
    public Iterable<Task> findAll() {
        return new LinkedList<>();
    }

    @Override
    public void delete(Long aLong) {

    }
}

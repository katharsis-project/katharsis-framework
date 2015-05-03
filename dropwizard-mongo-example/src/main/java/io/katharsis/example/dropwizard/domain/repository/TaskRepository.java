package io.katharsis.example.dropwizard.domain.repository;

import io.katharsis.example.dropwizard.domain.model.Task;
import io.katharsis.repository.ResourceRepository;

public class TaskRepository implements ResourceRepository<Task, Long> {
    public Task findOne(Long aLong) {
        return null;
    }

    public Iterable<Task> findAll() {
        return null;
    }

    public void delete(Long aLong) {

    }

    public <S extends Task> S update(S s) {
        return null;
    }

    public <S extends Task> S save(S s) {
        return null;
    }
}

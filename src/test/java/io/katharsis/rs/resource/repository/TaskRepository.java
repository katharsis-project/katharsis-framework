package io.katharsis.rs.resource.repository;

import io.katharsis.repository.EntityRepository;
import io.katharsis.rs.resource.model.Task;
import org.jvnet.hk2.annotations.Service;

import java.util.Arrays;

@Service
public class TaskRepository implements EntityRepository<Task, Long> {
    @Override
    public <S extends Task> S save(S entity) {
        return null;
    }

    @Override
    public Task findOne(Long aLong) {
        return new Task(aLong, "Task");
    }

    @Override
    public Iterable<Task> findAll() {
        return Arrays.asList(new Task(1L, "First task"));
    }

    @Override
    public void delete(Long aLong) {

    }
}

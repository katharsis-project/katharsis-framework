package io.katharsis.resource.mock.repository;

import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.mock.models.Project;

import java.util.HashMap;
import java.util.Map;

public class ProjectRepository implements ResourceRepository<Project, Long> {

    // Used ThreadLocal in case of switching to TestNG and using concurrent tests
    private static final ThreadLocal<Map<Long, Project>> repository = new ThreadLocal<Map<Long, Project>>() {
        @Override
        protected Map<Long, Project> initialValue() {
            return new HashMap<>();
        }
    };

    @Override
    public <S extends Project> S save(S entity) {
        entity.setId((long) (repository.get().size() + 1));
        repository.get().put(entity.getId(), entity);

        return entity;
    }

    @Override
    public <S extends Project> S update(S entity) {
        repository.get().put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Project findOne(Long aLong) {
        return repository.get().get(aLong);
    }

    @Override
    public Iterable<Project> findAll() {
        return repository.get().values();
    }

    @Override
    public void delete(Long aLong) {
        repository.get().remove(aLong);
    }
}

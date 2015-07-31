package io.katharsis.resource.mock.repository;

import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.mock.models.Project;

import java.util.HashMap;
import java.util.Map;

public class ProjectRepository implements ResourceRepository<Project, Long> {

    // Used ThreadLocal in case of switching to TestNG and using concurrent tests
    private static final ThreadLocal<Map<Long, Project>> THREAD_LOCAL_REPOSITORY = new ThreadLocal<Map<Long, Project>>() {
        @Override
        protected Map<Long, Project> initialValue() {
            return new HashMap<>();
        }
    };

    @Override
    public <S extends Project> S save(S entity) {
        entity.setId((long) (THREAD_LOCAL_REPOSITORY.get().size() + 1));
        THREAD_LOCAL_REPOSITORY.get().put(entity.getId(), entity);

        return entity;
    }

    @Override
    public Project findOne(Long aLong, RequestParams requestParams) {
        Project project = THREAD_LOCAL_REPOSITORY.get().get(aLong);
        if (project == null) {
            throw new ResourceNotFoundException(Project.class.getCanonicalName());
        }
        return project;
    }

    @Override
    public Iterable<Project> findAll(RequestParams requestParams) {
        return THREAD_LOCAL_REPOSITORY.get().values();
    }


    @Override
    public Iterable<Project> findAll(Iterable<Long> longs, RequestParams requestParams) {
        return null;
    }

    @Override
    public void delete(Long aLong) {
        THREAD_LOCAL_REPOSITORY.get().remove(aLong);
    }
}

package io.katharsis.example.dropwizardSimple.domain.repository;

import com.google.common.collect.Iterables;
import io.katharsis.example.dropwizardSimple.domain.model.Project;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.exception.ResourceNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ProjectRepository implements ResourceRepository<Project, Long> {

    private static final Map<Long, Project> REPOSITORY = new ConcurrentHashMap<>();
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    public <S extends Project> S save(S entity) {
        if (entity.getId() == null) {
            entity.setId(ID_GENERATOR.getAndIncrement());
        }
        REPOSITORY.put(entity.getId(), entity);
        return entity;
    }

    public Project findOne(Long id, QueryParams requestParams) {
        Project project = REPOSITORY.get(id);
        if (project == null) {
            throw new ResourceNotFoundException("Project not found");
        }
        return project;
    }

    @Override
    public Iterable<Project> findAll(QueryParams requestParams) {
        return REPOSITORY.values();
    }

    @Override
    public Iterable<Project> findAll(Iterable<Long> iterable, QueryParams requestParams) {
        Set<Map.Entry<Long, Project>> entries = REPOSITORY.entrySet();
        Map<Long, Project> map = new HashMap<>();
        for (Map.Entry<Long, Project> entry: entries) {
            if (Iterables.contains(iterable, entry.getKey())) {
                map.put(entry.getKey(), entry. getValue());
            }
        }
        return map.values();
    }

    public void delete(Long id) {
        REPOSITORY.remove(id);
    }
}
package io.katharsis.example.dropwizardSimple.domain.repository;

import com.google.common.collect.Iterables;
import io.katharsis.example.dropwizardSimple.domain.model.Project;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.exception.ResourceNotFoundException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
        return REPOSITORY.entrySet()
                .stream()
                .filter(p -> Iterables.contains(iterable, p.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                .values();
    }

    public void delete(Long id) {
        REPOSITORY.remove(id);
    }
}
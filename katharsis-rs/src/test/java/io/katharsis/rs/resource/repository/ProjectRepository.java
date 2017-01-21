package io.katharsis.rs.resource.repository;

import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.legacy.repository.ResourceRepository;
import io.katharsis.rs.resource.model.Project;

public class ProjectRepository implements ResourceRepository<Project, Long> {
    @Override
    public <S extends Project> S save(S entity) {
        return null;
    }

    @Override
    public Project findOne(Long aLong, QueryParams requestParams) {
        return null;
    }

    @Override
    public Iterable<Project> findAll(QueryParams requestParams) {
        return null;
    }

    @Override
    public Iterable<Project> findAll(Iterable<Long> longs, QueryParams requestParams) {
        return null;
    }

    @Override
    public void delete(Long aLong) {

    }
}

package io.katharsis.example.jersey.domain.repository;

import io.katharsis.example.jersey.domain.model.Project;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.ResourceRepository;

public class ProjectRepository implements ResourceRepository<Project, Long> {
    @Override
    public <S extends Project> S save(S entity) {
        return null;
    }

    @Override
    public Project findOne(Long aLong) {
        return null;
    }

    @Override
    public Iterable<Project> findAll(RequestParams requestParams) {
        return null;
    }

    @Override
    public Iterable<Project> findAll(Iterable<Long> projectIds, RequestParams requestParams) {
        return null;
    }

    @Override
    public void delete(Long aLong) {

    }
}

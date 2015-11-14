package io.katharsis.spring.domain.repository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.spring.domain.model.Project;
import org.springframework.stereotype.Component;

@Component
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
    public Iterable<Project> findAll(Iterable<Long> projectIds, QueryParams requestParams) {
        return null;
    }

    @Override
    public void delete(Long aLong) {

    }
}

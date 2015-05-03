package io.katharsis.rs.resource.repository;

import io.katharsis.repository.ResourceRepository;
import io.katharsis.rs.resource.model.Project;

public class ProjectRepository implements ResourceRepository<Project, Long> {
    @Override
    public <S extends Project> S save(S entity) {
        return null;
    }

    @Override
    public <S extends Project> S update(S s) {
        return null;
    }

    @Override
    public Project findOne(Long aLong) {
        return null;
    }

    @Override
    public Iterable<Project> findAll() {
        return null;
    }

    @Override
    public void delete(Long aLong) {

    }
}

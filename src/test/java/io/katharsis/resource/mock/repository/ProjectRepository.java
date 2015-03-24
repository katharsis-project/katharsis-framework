package io.katharsis.resource.mock.repository;

import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.mock.models.Project;

import java.util.LinkedList;

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
    public Iterable<Project> findAll() {
        return new LinkedList<>();
    }

    @Override
    public void delete(Long aLong) {

    }
}

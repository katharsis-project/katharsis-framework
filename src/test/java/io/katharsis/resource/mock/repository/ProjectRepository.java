package io.katharsis.resource.mock.repository;

import io.katharsis.repository.EntityRepository;
import io.katharsis.resource.mock.models.Project;

import java.util.LinkedList;

public class ProjectRepository implements EntityRepository<Project, Long> {
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

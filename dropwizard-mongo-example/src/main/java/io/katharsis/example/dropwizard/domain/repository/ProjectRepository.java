package io.katharsis.example.dropwizard.domain.repository;

import io.katharsis.example.dropwizard.domain.model.Project;
import io.katharsis.repository.ResourceRepository;

public class ProjectRepository implements ResourceRepository<Project, Long> {
    public <S extends Project> S save(S entity) {
        return null;
    }

    public <S extends Project> S update(S s) {
        return null;
    }

    public Project findOne(Long aLong) {
        return null;
    }

    public Iterable<Project> findAll() {
        return null;
    }

    public void delete(Long aLong) {
    }
}
package io.katharsis.rs.resource.repository;

import io.katharsis.repository.EntityRepository;
import io.katharsis.rs.resource.model.Project;
import org.jvnet.hk2.annotations.Service;

@Service
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
        return null;
    }

    @Override
    public void delete(Long aLong) {

    }
}

package io.katharsis.vertx.examples.repository;


import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.vertx.examples.domain.Project;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class ProjectRepository implements ResourceRepository<Project, Long> {

    @Override
    public Project findOne(Long aLong, QueryParams queryParams) {
        log.info("Find all {} {}", aLong, queryParams);
        return Project.builder().id(aLong).name("ProfilesRD").build();
    }

    @Override
    public Iterable<Project> findAll(QueryParams queryParams) {
        log.info("FInd all {}", queryParams);
        return findAll(null, queryParams);
    }

    @Override
    public Iterable<Project> findAll(Iterable<Long> longs, QueryParams queryParams) {
        log.info("Find all {} {}", longs, queryParams);
        return Arrays.asList(
                Project.builder().id(1L).name("ProfilesRD").build(),
                Project.builder().id(2L).name("Great people inside").build()
        );
    }

    @Override
    public void delete(Long aLong) {
        log.info("Delete {}", aLong);
    }

    @Override
    public <S extends Project> S save(S entity) {
        log.info("Save project {}", entity);
        return entity;
    }
}

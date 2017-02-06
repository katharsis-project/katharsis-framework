package io.katharsis.vertx.examples.repository;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.legacy.repository.ResourceRepository;
import io.katharsis.vertx.examples.domain.Project;

public class ProjectRepository implements ResourceRepository<Project, Long> {

  private Logger log = LoggerFactory.getLogger(ProjectRepository.class);

  @Override
  public Project findOne(Long aLong, QueryParams queryParams) {
    log.info("Find all {} {}", aLong, queryParams);
    return new Project(aLong, "ProfilesRD");
  }

  @Override
  public Iterable<Project> findAll(QueryParams queryParams) {
    log.info("FInd all {}", queryParams);
    return findAll(null, queryParams);
  }

  @Override
  public Iterable<Project> findAll(Iterable<Long> longs, QueryParams queryParams) {
    log.info("Find all {} {}", longs, queryParams);
    return Arrays.asList(new Project(1L, "ProfilesRD"), new Project(2L, "Great people inside"));
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

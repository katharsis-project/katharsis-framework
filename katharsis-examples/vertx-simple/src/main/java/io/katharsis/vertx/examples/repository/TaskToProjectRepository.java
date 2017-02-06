package io.katharsis.vertx.examples.repository;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.legacy.repository.RelationshipRepository;
import io.katharsis.vertx.examples.domain.Project;
import io.katharsis.vertx.examples.domain.Task;

public class TaskToProjectRepository implements RelationshipRepository<Task, Long, Project, Long> {

  private Logger log = LoggerFactory.getLogger(TaskToProjectRepository.class);

  @Override
  public void setRelation(Task source, Long targetId, String fieldName) {
    log.info("Set relation {} {} {}", source, targetId, fieldName);
  }

  @Override
  public void setRelations(Task source, Iterable<Long> targetIds, String fieldName) {
    log.info("Set relationS {} {} {}", source, targetIds, fieldName);
  }

  @Override
  public void addRelations(Task source, Iterable<Long> targetIds, String fieldName) {
    log.info("Add relations {} {} {}", source, targetIds, fieldName);
  }

  @Override
  public void removeRelations(Task source, Iterable<Long> targetIds, String fieldName) {
    log.info("Remove relations {} {} {}", source, targetIds, fieldName);
  }

  @Override
  public Project findOneTarget(Long sourceId, String fieldName, QueryParams queryParams) {
    log.info("Find one target {} {} {}", sourceId, fieldName, queryParams);
    return new Project(sourceId, "find one target " + fieldName);
  }

  @Override
  public Iterable<Project> findManyTargets(Long sourceId, String fieldName, QueryParams queryParams) {
    log.info("Find many targets {} {} {}", sourceId, fieldName, queryParams);
    return Arrays.asList(new Project(sourceId, "find many targets " + fieldName));
  }
}

package io.katharsis.vertx.examples.repository;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.katharsis.legacy.queryParams.QueryParams;
import io.katharsis.legacy.repository.ResourceRepository;
import io.katharsis.vertx.examples.domain.Task;

public class TaskRepository implements ResourceRepository<Task, Long> {

  private Logger log = LoggerFactory.getLogger(TaskRepository.class);

  @Override
  public Task findOne(Long aLong, QueryParams queryParams) {
    log.info("Find one {} {}", aLong, queryParams);
    return new Task(aLong, "Some task " + aLong);
  }

  @Override
  public Iterable<Task> findAll(QueryParams queryParams) {
    log.info("find all {}", queryParams);
    return findAll(null, queryParams);
  }

  @Override
  public Iterable<Task> findAll(Iterable<Long> longs, QueryParams queryParams) {
    log.info("find all {} {}", longs, queryParams);
    return Arrays.asList(new Task(1L, "First task"));
  }

  @Override
  public void delete(Long aLong) {
    log.info("Delete Task {}", aLong);
  }

  @Override
  public <S extends Task> S save(S entity) {
    log.info("Save task {}", entity);
    return entity;
  }
}

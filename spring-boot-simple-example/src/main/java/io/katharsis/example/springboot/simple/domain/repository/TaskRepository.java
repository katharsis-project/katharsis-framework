/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.katharsis.example.springboot.simple.domain.repository;

import com.google.common.collect.Iterables;
import io.katharsis.example.springboot.simple.domain.model.Task;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class TaskRepository implements ResourceRepository<Task, Long> {
    private static final Map<Long, Task> REPOSITORY = new ConcurrentHashMap<>();
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    @Override
    public <S extends Task> S save(S entity) {
        if (entity.getId() == null) {
            entity.setId(ID_GENERATOR.getAndIncrement());
        }
        REPOSITORY.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Task findOne(Long taskId, QueryParams requestParams) {
        Task task = REPOSITORY.get(taskId);
        if (task == null) {
            throw new ResourceNotFoundException("Project not found!");
        }
        return task;
    }

    @Override
    public Iterable<Task> findAll(QueryParams requestParams) {
        return REPOSITORY.values();
    }

    @Override
    public Iterable<Task> findAll(Iterable<Long> taskIds, QueryParams requestParams) {
        return REPOSITORY.entrySet()
                .stream()
                .filter(p -> Iterables.contains(taskIds, p.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                .values();
    }

    @Override
    public void delete(Long taskId) {
        REPOSITORY.remove(taskId);
    }
}

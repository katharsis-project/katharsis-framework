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

import io.katharsis.example.springboot.simple.domain.model.Task;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;

import java.util.Arrays;

import org.springframework.stereotype.Component;

@Component
public class TaskRepository implements ResourceRepository<Task, Long> {
    @Override
    public <S extends Task> S save(S entity) {
        return null;
    }

    @Override
    public Task findOne(Long aLong, QueryParams requestParams) {
        Task task = new Task(aLong, "Some task");
        return task;
    }

    @Override
    public Iterable<Task> findAll(QueryParams requestParams) {
        return findAll(null, requestParams);
    }

    @Override
    public Iterable<Task> findAll(Iterable<Long> taskIds, QueryParams requestParams) {
        return Arrays.asList(new Task(1L, "First task"));
    }

    @Override
    public void delete(Long aLong) {

    }
}

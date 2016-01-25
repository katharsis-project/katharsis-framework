package io.katharsis.spring.domain.repository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.annotations.JsonApiFindAll;
import io.katharsis.repository.annotations.JsonApiFindAllWithIds;
import io.katharsis.repository.annotations.JsonApiFindOne;
import io.katharsis.repository.annotations.JsonApiResourceRepository;
import io.katharsis.spring.domain.model.Task;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Collections;
import java.util.TimeZone;

@Component
@JsonApiResourceRepository(Task.class)
public class TaskRepository {

    @JsonApiFindAll
    public Iterable<Task> findAll(TimeZone timeZone, QueryParams queryParams) {
        return findAll(null, timeZone, queryParams);
    }

    @JsonApiFindAllWithIds
    public Iterable<Task> findAll(Iterable<Long> taskIds, TimeZone timeZone, QueryParams queryParams) {
        String name = queryParams.getFilters().getParams().get("Task").getParams().get("name").iterator().next();
        return Collections.singletonList(new Task(1L, name));
    }
}

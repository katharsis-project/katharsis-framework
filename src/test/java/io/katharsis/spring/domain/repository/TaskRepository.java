package io.katharsis.spring.domain.repository;

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

    @JsonApiFindOne
    public Task findOne(Long aLong, @RequestHeader("User-Agent") String userAgent) {
        return new Task(aLong, userAgent);
    }

    @JsonApiFindAll
    public Iterable<Task> findAll(TimeZone timeZone) {
        return findAll(null, timeZone);
    }

    @JsonApiFindAllWithIds
    public Iterable<Task> findAll(Iterable<Long> taskIds, TimeZone timeZone) {
        return Collections.singletonList(new Task(1L, timeZone.getDisplayName()));
    }
}

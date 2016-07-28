package io.katharsis.spring.domain.repository;

import io.katharsis.repository.annotations.JsonApiFindAll;
import io.katharsis.repository.annotations.JsonApiResourceRepository;
import io.katharsis.response.JsonApiResponse;
import io.katharsis.response.MetaInformation;
import io.katharsis.spring.domain.model.Task;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@JsonApiResourceRepository(Task.class)
public class TaskRepository {

    @JsonApiFindAll
    public JsonApiResponse findAll() {
        return new JsonApiResponse()
            .setEntity(Collections.singletonList(new Task(1L, "John")))
            .setMetaInformation(new MetaInformation() {
                public String name = "meta information";
            });
    }
}

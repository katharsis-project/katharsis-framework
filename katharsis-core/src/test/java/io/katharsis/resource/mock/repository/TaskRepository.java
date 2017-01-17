package io.katharsis.resource.mock.repository;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.annotations.JsonApiDelete;
import io.katharsis.repository.annotations.JsonApiFindAll;
import io.katharsis.repository.annotations.JsonApiFindAllWithIds;
import io.katharsis.repository.annotations.JsonApiFindOne;
import io.katharsis.repository.annotations.JsonApiMeta;
import io.katharsis.repository.annotations.JsonApiResourceRepository;
import io.katharsis.repository.annotations.JsonApiSave;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.mock.models.Task;
import io.katharsis.response.MetaInformation;

@JsonApiResourceRepository(Task.class)
public class TaskRepository {

    private static final ConcurrentHashMap<Long, Task> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();
    
    public static void clear(){
    	THREAD_LOCAL_REPOSITORY.clear();
    }

    @JsonApiSave
    public <S extends Task> S save(S entity) {
        if (entity.getId() == null) {
            entity.setId((long) (THREAD_LOCAL_REPOSITORY.size() + 1));
        }
        THREAD_LOCAL_REPOSITORY.put(entity.getId(), entity);

        return entity;
    }

    @JsonApiFindOne
    public Task findOne(Long aLong, QueryParams queryParams) {
        Task task = THREAD_LOCAL_REPOSITORY.get(aLong);
        if (task == null) {
            throw new ResourceNotFoundException("");
        }
        return task;
    }

    @JsonApiFindAll
    public Iterable<Task> findAll(QueryParams queryParams) {
        return THREAD_LOCAL_REPOSITORY.values();
    }


    @JsonApiFindAllWithIds
    public Iterable<Task> findAll(Iterable<Long> ids, QueryParams queryParams) {
        List<Task> values = new LinkedList<>();
        for (Task value : THREAD_LOCAL_REPOSITORY.values()) {
            if (contains(value, ids)) {
                values.add(value);
            }
        }
        return values;
    }

    private boolean contains(Task value, Iterable<Long> ids) {
        for (Long id : ids) {
            if (value.getId().equals(id)) {
                return true;
            }
        }

        return false;
    }

    @JsonApiDelete
    public void delete(Long aLong) {
        THREAD_LOCAL_REPOSITORY.remove(aLong);
    }

    @JsonApiMeta
    public MetaInformation getMetaInformation(Iterable<Task> resources, QueryParams queryParams) {
        return new MetaData();
    }

    public static class MetaData implements MetaInformation {
    	
    	public String someValue;
    }
}

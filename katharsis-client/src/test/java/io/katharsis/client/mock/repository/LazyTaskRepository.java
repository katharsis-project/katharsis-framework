package io.katharsis.client.mock.repository;

import io.katharsis.client.mock.models.LazyTask;
import io.katharsis.client.mock.models.Project;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.exception.ResourceNotFoundException;

import java.util.concurrent.ConcurrentHashMap;

public class LazyTaskRepository implements ResourceRepository<LazyTask, Long> {

    private static final ConcurrentHashMap<Long, LazyTask> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

    public static void clear(){
    	THREAD_LOCAL_REPOSITORY.clear();
    }
    
    @Override
    public <S extends LazyTask> S save(S entity) {
        entity.setId((long) (THREAD_LOCAL_REPOSITORY.size() + 1));
        THREAD_LOCAL_REPOSITORY.put(entity.getId(), entity);

        return entity;
    }

    @Override
    public LazyTask findOne(Long aLong, QueryParams queryParams) {
        LazyTask task = THREAD_LOCAL_REPOSITORY.get(aLong);
        if (task == null) {
            throw new ResourceNotFoundException(Project.class.getCanonicalName());
        }
        return task;
    }

    @Override
    public Iterable<LazyTask> findAll(QueryParams queryParamss) {
        return THREAD_LOCAL_REPOSITORY.values();
    }


    @Override
    public Iterable<LazyTask> findAll(Iterable<Long> ids, QueryParams queryParams) {
        return THREAD_LOCAL_REPOSITORY.values();
    }

    @Override
    public void delete(Long aLong) {
        THREAD_LOCAL_REPOSITORY.remove(aLong);
    }
}

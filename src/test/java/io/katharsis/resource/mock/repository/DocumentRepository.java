package io.katharsis.resource.mock.repository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.mock.models.Document;

import java.util.concurrent.ConcurrentHashMap;

public class DocumentRepository implements ResourceRepository<Document, Long> {

    private static final ConcurrentHashMap<Long, Document> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

    @Override
    public <S extends Document> S save(S entity) {
        entity.setId((long) (THREAD_LOCAL_REPOSITORY.size() + 1));
        THREAD_LOCAL_REPOSITORY.put(entity.getId(), entity);

        return entity;
    }

    @Override
    public Document findOne(Long aLong, QueryParams queryParams) {
        Document project = THREAD_LOCAL_REPOSITORY.get(aLong);
        if (project == null) {
            throw new ResourceNotFoundException(Document.class.getCanonicalName());
        }
        return project;
    }

    @Override
    public Iterable<Document> findAll(QueryParams queryParams) {
        return THREAD_LOCAL_REPOSITORY.values();
    }

    @Override
    public Iterable<Document> findAll(Iterable<Long> longs, QueryParams queryParams) {
        return null;
    }

    @Override
    public void delete(Long aLong) {
        THREAD_LOCAL_REPOSITORY.remove(aLong);
    }
}

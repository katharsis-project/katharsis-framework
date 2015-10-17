package io.katharsis.resource.mock.repository;

import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.mock.models.Document;

import java.util.HashMap;
import java.util.Map;

public class DocumentRepository implements ResourceRepository<Document, Long> {

    // Used ThreadLocal in case of switching to TestNG and using concurrent tests
    private static final ThreadLocal<Map<Long, Document>> THREAD_LOCAL_REPOSITORY = new ThreadLocal<Map<Long, Document>>() {
        @Override
        protected Map<Long, Document> initialValue() {
            return new HashMap<>();
        }
    };

    @Override
    public <S extends Document> S save(S entity) {
        entity.setId((long) (THREAD_LOCAL_REPOSITORY.get().size() + 1));
        THREAD_LOCAL_REPOSITORY.get().put(entity.getId(), entity);

        return entity;
    }

    @Override
    public Document findOne(Long aLong, RequestParams requestParams) {
        Document project = THREAD_LOCAL_REPOSITORY.get().get(aLong);
        if (project == null) {
            throw new ResourceNotFoundException(Document.class.getCanonicalName());
        }
        return project;
    }

    @Override
    public Iterable<Document> findAll(RequestParams requestParams) {
        return THREAD_LOCAL_REPOSITORY.get().values();
    }

    @Override
    public void delete(Long aLong) {
        THREAD_LOCAL_REPOSITORY.get().remove(aLong);
    }
}

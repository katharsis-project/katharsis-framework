package io.katharsis.resource.mock.repository;

import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.mock.models.Document;

public class DocumentRepository implements ResourceRepository<Document, Long> {
    @Override
    public Document findOne(Long aLong, RequestParams requestParams) {
        return null;
    }

    @Override
    public Iterable<Document> findAll(RequestParams requestParams) {
        return null;
    }

    @Override
    public Iterable<Document> findAll(Iterable<Long> longs, RequestParams requestParams) {
        return null;
    }

    @Override
    public <S extends Document> S save(S entity) {
        return null;
    }

    @Override
    public void delete(Long aLong) {

    }
}

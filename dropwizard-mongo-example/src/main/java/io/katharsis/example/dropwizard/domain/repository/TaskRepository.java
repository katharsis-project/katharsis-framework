package io.katharsis.example.dropwizard.domain.repository;

import io.katharsis.example.dropwizard.domain.model.Task;
import io.katharsis.example.dropwizard.managed.MongoManaged;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;

import javax.inject.Inject;

public class TaskRepository implements ResourceRepository<Task, ObjectId> {
    private Datastore datastore;

    @Inject
    public TaskRepository(MongoManaged mongoManaged) {
        datastore = mongoManaged.getDatastore();
    }

    public <S extends Task> S save(S entity) {
        Key<Task> saveKey = datastore.save(entity);
        return (S) datastore.getByKey(Task.class, saveKey);
    }

    public Task findOne(ObjectId id, QueryParams requestParams) {
        return datastore.getByKey(Task.class, new Key<>(Task.class, id));
    }

    @Override
    public Iterable<Task> findAll(QueryParams requestParams) {
        return datastore.find(Task.class);
    }

    @Override
    public Iterable<Task> findAll(Iterable<ObjectId> iterable, QueryParams requestParams) {
        return datastore.get(Task.class, iterable);
    }

    public void delete(ObjectId id) {
        datastore.delete(datastore.createQuery(Task.class).filter("_id", id));
    }
}

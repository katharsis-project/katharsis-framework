package io.katharsis.resource.mock.repository;

import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.mock.models.User;

import java.util.HashMap;
import java.util.Map;

public class UserRepository implements ResourceRepository<User, Long> {

    // Used ThreadLocal in case of switching to TestNG and using concurrent tests
    private static final ThreadLocal<Map<Long, User>> repository = new ThreadLocal<Map<Long, User>>() {
        @Override
        protected Map<Long, User> initialValue() {
            return new HashMap<>();
        }
    };

    @Override
    public <S extends User> S save(S entity) {
        entity.setId((long) (repository.get().size() + 1));
        repository.get().put(entity.getId(), entity);

        return entity;
    }

    @Override
    public <S extends User> S update(S entity) {
        repository.get().put(entity.getId(), entity);
        return entity;
    }

    @Override
    public User findOne(Long aLong) {
        return repository.get().get(aLong);
    }

    @Override
    public Iterable<User> findAll() {
        return repository.get().values();
    }

    @Override
    public void delete(Long aLong) {
        repository.get().remove(aLong);
    }
}

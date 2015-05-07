package io.katharsis.resource.mock.repository;

import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.mock.models.User;

import java.util.HashMap;
import java.util.Map;

public class UserRepository implements ResourceRepository<User, Long> {

    // Used ThreadLocal in case of switching to TestNG and using concurrent tests
    private static final ThreadLocal<Map<Long, User>> THREAD_LOCAL_REPOSITORY = new ThreadLocal<Map<Long, User>>() {
        @Override
        protected Map<Long, User> initialValue() {
            return new HashMap<>();
        }
    };

    @Override
    public <S extends User> S save(S entity) {
        entity.setId((long) (THREAD_LOCAL_REPOSITORY.get().size() + 1));
        THREAD_LOCAL_REPOSITORY.get().put(entity.getId(), entity);

        return entity;
    }

    @Override
    public <S extends User> S update(S entity) {
        THREAD_LOCAL_REPOSITORY.get().put(entity.getId(), entity);
        return entity;
    }

    @Override
    public User findOne(Long aLong) {
        User user = THREAD_LOCAL_REPOSITORY.get().get(aLong);
        if (user == null) {
            throw new ResourceNotFoundException("");
        }
        return user;
    }

    @Override
    public Iterable<User> findAll() {
        return THREAD_LOCAL_REPOSITORY.get().values();
    }

    @Override
    public void delete(Long aLong) {
        THREAD_LOCAL_REPOSITORY.get().remove(aLong);
    }
}

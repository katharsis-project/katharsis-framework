package io.katharsis.resource.mock.repository;

import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.mock.models.User;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UserRepository implements ResourceRepository<User, Long> {

    private static final QueryParams REQUEST_PARAMS = new QueryParams();

    private static final ConcurrentHashMap<Long, User> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

    @Override
    public <S extends User> S save(S entity) {
        entity.setId((long) (THREAD_LOCAL_REPOSITORY.size() + 1));
        THREAD_LOCAL_REPOSITORY.put(entity.getId(), entity);

        return entity;
    }

    @Override
    public User findOne(Long aLong, QueryParams queryParams) {
        User user = THREAD_LOCAL_REPOSITORY.get(aLong);
        if (user == null) {
            throw new ResourceNotFoundException(User.class.getCanonicalName());
        }

        return user;
    }

    @Override
    public Iterable<User> findAll(QueryParams queryParams) {
        return THREAD_LOCAL_REPOSITORY.values();
    }


    @Override
    public Iterable<User> findAll(Iterable<Long> ids, QueryParams queryParams) {
        return THREAD_LOCAL_REPOSITORY.values()
            .stream()
            .filter(value -> contains(value, ids))
            .collect(Collectors.toList());
    }

    private boolean contains(User value, Iterable<Long> ids) {
        for (Long id : ids) {
            if (value.getId().equals(id)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void delete(Long aLong) {
        THREAD_LOCAL_REPOSITORY.remove(aLong);
    }
}

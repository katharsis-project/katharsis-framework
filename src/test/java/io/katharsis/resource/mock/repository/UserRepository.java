package io.katharsis.resource.mock.repository;

import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.mock.models.User;

public class UserRepository implements ResourceRepository<User, Long> {
    @Override
    public User findOne(Long aLong) {
        return null;
    }

    @Override
    public Iterable<User> findAll() {
        return null;
    }

    @Override
    public <S extends User> S save(S entity) {
        return null;
    }

    @Override
    public <S extends User> S update(S entity) {
        return null;
    }

    @Override
    public void delete(Long aLong) {

    }
}

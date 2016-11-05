package io.katharsis.resource.mock.repository;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.katharsis.queryspec.QuerySpec;
import io.katharsis.queryspec.QuerySpecResourceRepository;
import io.katharsis.resource.exception.ResourceNotFoundException;
import io.katharsis.resource.mock.models.User;

public class UserRepository implements QuerySpecResourceRepository<User, Long> {

    private static final ConcurrentHashMap<Long, User> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();
    
    public static void clear(){
    	THREAD_LOCAL_REPOSITORY.clear();
    }

    @Override
    public <S extends User> S save(S entity) {
        entity.setId((long) (THREAD_LOCAL_REPOSITORY.size() + 1));
        THREAD_LOCAL_REPOSITORY.put(entity.getId(), entity);

        return entity;
    }

    @Override
    public User findOne(Long aLong, QuerySpec queryParams) {
        User user = THREAD_LOCAL_REPOSITORY.get(aLong);
        if (user == null) {
            throw new ResourceNotFoundException(User.class.getCanonicalName());
        }

        return user;
    }

    @Override
    public Iterable<User> findAll(QuerySpec queryParams) {
        return THREAD_LOCAL_REPOSITORY.values();
    }


    @Override
    public Iterable<User> findAll(Iterable<Long> ids, QuerySpec queryParams) {
        List<User> values = new LinkedList<>();
        for (User value : THREAD_LOCAL_REPOSITORY.values()) {
            if (contains(value, ids)) {
                values.add(value);
            }
        }
        return values;
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

	@Override
	public Class<User> getResourceClass() {
		return User.class;
	}
}

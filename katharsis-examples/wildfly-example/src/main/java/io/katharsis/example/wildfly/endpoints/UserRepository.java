package io.katharsis.example.wildfly.endpoints;

import io.katharsis.example.wildfly.model.User;
import io.katharsis.queryParams.QueryParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.exception.ResourceNotFoundException;

import java.util.*;

public class UserRepository implements ResourceRepository<User, String> {

    private Set<User> users = new LinkedHashSet<>();

    public UserRepository() {
        List<String> interests = new ArrayList<>();
        interests.add("coding");
        interests.add("art");
        users.add(new User(UUID.randomUUID().toString(), "grogdj@gmail.com", "grogdj", "grogj", "dj", interests));
        users.add(new User(UUID.randomUUID().toString(), "bot@gmail.com", "bot", "bot", "harry", interests));
        users.add(new User(UUID.randomUUID().toString(), "evilbot@gmail.com", "evilbot", "bot", "john", interests));
    }

    @Override
    public synchronized User findOne(String id, QueryParams requestParams) {
        for (User user : users) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        throw new ResourceNotFoundException("users/" + id);
    }

    @Override
    public synchronized Iterable<User> findAll(QueryParams requestParams) {
        return users;
    }

    @Override
    public synchronized Iterable<User> findAll(Iterable<String> ids, QueryParams requestParams) {
        List<User> foundUsers = new ArrayList<>();
        for (User user : users) {
            for (String id: ids) {
                if (id.equals(user.getId())) {
                    foundUsers.add(user);
                }
            }
        }
        return foundUsers;
    }

    @Override
    public synchronized void delete(String id) {
        Iterator<User> usersIterator = users.iterator();

        while (usersIterator.hasNext()) {
            if (usersIterator.next().getId().equals(id)) {
                usersIterator.remove();
            }
        }
    }

    @Override
    public synchronized <S extends User> S save(S user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID().toString());
        }
        users.add(user);
        return user;
    }
}

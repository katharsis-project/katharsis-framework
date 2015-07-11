package io.katharsis.example.wildfly.endpoints;

import io.katharsis.example.wildfly.model.User;
import io.katharsis.queryParams.RequestParams;
import io.katharsis.repository.ResourceRepository;
import io.katharsis.resource.exception.ResourceNotFoundException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
    public User findOne(String id, RequestParams requestParams) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("users", "users/" + id));
    }

    @Override
    public Iterable<User> findAll(RequestParams requestParams) {
        return users;
    }

    @Override
    public Iterable<User> findAll(Iterable<String> ids, RequestParams requestParams) {
        return users.stream()
                .filter(u ->
                        StreamSupport.stream(ids.spliterator(), false)
                                .filter(id -> u.getId().equals(id))
                                .findFirst()
                                .isPresent())
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        Iterator<User> usersIterator = users.iterator();

        while (usersIterator.hasNext()) {
            if (usersIterator.next().getId().equals(id)) {
                usersIterator.remove();
            }
        }
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID().toString());
        }
        users.add(user);
        return user;
    }
}

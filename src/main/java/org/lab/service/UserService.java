package org.lab.service;

import org.lab.model.User;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class UserService {
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public User register(String name) {
        long id = idGenerator.getAndIncrement();
        User user = new User(id, name);
        users.put(id, user);
        return user;
    }

    public Optional<User> getUser(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public Collection<User> getAllUsers() {
        return users.values();
    }
}

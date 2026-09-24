package com.bazarfx.service;

import com.bazarfx.model.User;
import com.bazarfx.util.JsonStorage;
import com.bazarfx.util.PasswordUtil;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Handles signup/login against the local users.json file. */
public class AuthService {

    private static final String FILE = "users.json";
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    public AuthService() {
        List<User> loaded = JsonStorage.loadList(FILE, new TypeToken<List<User>>() {}.getType());
        for (User u : loaded) {
            users.put(u.getUsername(), u);
        }
    }

    /** Returns an error message, or null on success. */
    public String signup(String username, String email, String phone, String location, String rawPassword) {
        if (username == null || username.isBlank()) return "Username is required.";
        if (rawPassword == null || rawPassword.length() < 4) return "Password must be at least 4 characters.";
        if (users.containsKey(username)) return "That username is already taken.";

        User user = new User(username, email, phone, location, PasswordUtil.hash(rawPassword));
        users.put(username, user);
        persist();
        return null;
    }

    public Optional<User> login(String username, String rawPassword) {
        User user = users.get(username);
        if (user != null && PasswordUtil.matches(rawPassword, user.getPasswordHash())) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    private void persist() {
        JsonStorage.saveList(FILE, List.copyOf(users.values()));
    }
}

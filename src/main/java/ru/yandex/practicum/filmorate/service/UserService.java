package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class UserService {

    private final UserStorage userStorage;


    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User createUser(User user) {
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        return userStorage.update(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User findById(Long userId) {
        return userStorage.findById(userId);
    }

    public void clearUser() {
        userStorage.clearUsers();
    }

    public void deleteUserById(Long userId) {
        userStorage.deleteUserById(userId);
    }
}

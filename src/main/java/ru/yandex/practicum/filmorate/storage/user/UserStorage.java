package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User create(User user);

    User update(User newUser);

    List<User> getAllUsers();

    void clearUsers();

    User findById(Long userId);

    void deleteUserById(Long userId);
}

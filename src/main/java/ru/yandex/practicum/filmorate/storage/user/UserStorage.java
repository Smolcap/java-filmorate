package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User create(User user);

    User update(User newUser);

    List<User> getAllUsers();

    void clearUsers();

    Optional<User> findById(Long userId);

    boolean deleteUserById(Long userId);
}

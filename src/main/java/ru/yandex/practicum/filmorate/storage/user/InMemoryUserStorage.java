package ru.yandex.practicum.filmorate.storage.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validaton.UserValidation;

import java.util.*;

@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private static Logger log = LoggerFactory.getLogger(InMemoryUserStorage.class);

    private Map<Long, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        UserValidation.validationForUser(user);
        user.setId(generationId());
        user.setFriends(new HashSet<>());

        users.put(user.getId(), user);

        log.info("Пользователь создан {} и добавлен в хранилище {}", user, user.getId());
        return user;
    }

    @Override
    public User update(User newUser) {
        UserValidation.validationForUser(newUser);
        validationUpdateUser(newUser);
        users.put(newUser.getId(), newUser);
        log.info("Пользователь обновлён с Id: {}", newUser.getId());
        return newUser;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void clearUsers() {
        users.clear();
    }

    @Override
    public Optional<User> findById(Long userId) {
        User user = users.get(userId);
        return Optional.ofNullable(user);
    }

    @Override
    public boolean deleteUserById(Long userId) {
        User removeUser = users.get(userId);
        if (removeUser != null) {
            users.remove(userId);
            log.info("Пользователь с ID {} успешно удалён", userId);
            return true;
        } else {
            log.error("Пользователь с ID {} для удаления не найден", userId);
            throw new NotFoundException("Пользователь для удаления с ID " + userId + " не найден");
        }
    }

    private void validationUpdateUser(User newUser) {
        if (newUser.getId() == null) {
            log.debug("Id не указан для обновления");
            throw new ValidationException("Id должен быть указан");
        }

        if (!users.containsKey(newUser.getId())) {
            log.warn("Id пользователя не найден {}", newUser.getId());
            throw new NotFoundException("Пользователь с таким Id не найден");
        }
    }

    private long generationId() {
        long currentMaxId = users.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++currentMaxId;
    }
}

package ru.yandex.practicum.filmorate.storage.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private static Logger log = LoggerFactory.getLogger(InMemoryUserStorage.class);

    private Map<Long, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        validation(user);
        user.setId(generationId());
        user.setFriends(new HashSet<>());

        users.put(user.getId(), user);

        log.info("Пользователь создан {} и добавлен в хранилище {}", user, user.getId());
        return user;
    }

    @Override
    public User update(User newUser) {
        validation(newUser);
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

    private void validation(User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty() || user.getEmail().isBlank() ||
                !user.getEmail().contains("@")) {
            log.warn("Электронная почта пользователя {}", user.getEmail());
            throw new ValidationException("электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().isEmpty()) {
            log.warn("Логин пользователя пуст");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if (user.getName() == null || user.getName().trim().isEmpty() || user.getName().isBlank()) {
            log.debug("Имя пользователя пустое, используется логин: {}", user.getLogin());
            user.setName(user.getLogin());
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

package ru.yandex.practicum.filmorate.storage.friend;

import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FriendshipService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component("inMemoryFriend")
public class InMemoryFriend implements FriendStorage {

    private static Logger log = LoggerFactory.getLogger(FriendshipService.class);

    private final UserStorage userStorage;

    @Autowired
    public InMemoryFriend(@Qualifier("inMemoryUserStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public Set<Long> addFriends(@NotNull Long userId, @NotNull Long friendId) {
        log.info("Попытка добавить друга: пользователь ID: {}, друг ID: {}", userId, friendId);

        Optional<User> userOptional = userStorage.findById(userId);
        Optional<User> friendOptional = userStorage.findById(friendId);

        if (userOptional.isEmpty() || friendOptional.isEmpty()) {
            log.error("Пользователь с ID {} или друг с ID {} не найдены", userId, friendId);
            throw new NotFoundException("Пользователь с ID: " + userId + " или друг с ID " + friendId + " не найдены");
        }

        User user = userOptional.get();
        User userFriend = friendOptional.get();

        if (user.getFriends().contains(friendId)) {
            log.error("Пользователь с ID {} уже является другом", friendId);
            throw new ValidationException("Пользователь уже является другом");
        }

        user.getFriends().add(friendId);
        userFriend.getFriends().add(userId);
        userStorage.update(user);
        userStorage.update(userFriend);

        log.info("Пользователь {} успешно добавил в друзья пользователя {}", userId, friendId);
        log.info("Список друзей пользователя {}: {}", user.getName(), user.getFriends());
        log.info("Список друзей пользователя {}: {}", userFriend.getName(), userFriend.getFriends());

        return user.getFriends();
    }

    @Override
    public Set<Long> deleteFromFriends(@NotNull Long userId, @NotNull Long friendId) {
        log.info("Попытка удалить друга: пользователь ID: {}, друг ID: {}", userId, friendId);

        Optional<User> userOptional = userStorage.findById(userId);
        Optional<User> friendOptional = userStorage.findById(friendId);

        if (userOptional.isEmpty() || friendOptional.isEmpty()) {
            log.error("Пользователь с ID {} или друг с ID {} не найдены", userId, friendId);
            throw new NotFoundException("Пользователь или друг с ID: " + userId + " " + friendId + " не найдены");
        }

        User user = userOptional.get();
        User userFriend = friendOptional.get();

        if (!user.getFriends().contains(friendId)) {
            log.error("Пользователь с ID {} не является другом пользователя с ID {}", friendId, userId);
            throw new ValidationException("Пользователь не является другом");
        }

        user.getFriends().remove(friendId);
        userFriend.getFriends().remove(userId);

        userStorage.update(user);
        userStorage.update(userFriend);

        log.info("Пользователь {} успешно удалил пользователя {} из своих друзей", userId, friendId);
        log.info("Список друзей пользователя {}: {}", user.getName(), user.getFriends());
        log.info("Список друзей пользователя {}: {}", userFriend.getName(), userFriend.getFriends());

        return user.getFriends();
    }

    @Override
    public List<User> findAllFriend(@NotNull Long userId) {
        log.info("Пользователь {} запрашивает список друзей", userId);

        Optional<User> userOptional = userStorage.findById(userId);

        if (userOptional.isEmpty()) {
            log.error("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }

        User user = userOptional.get();
        log.info("Список друзей пользователя {}: {}", userId, user.getFriends());

        Set<Long> friendsId = user.getFriends();

        List<User> friendList = friendsId.stream()
                .map(friendId -> userStorage.findById(friendId)
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return friendList;
    }


    @Override
    public List<User> listMutualFriend(@NotNull Long userId, @NotNull Long friendId) {
        log.info("Попытка найти общих друзей между пользователями с ID: {} и {}", userId, friendId);

        Optional<User> userOptional = userStorage.findById(userId);
        Optional<User> friendOptional = userStorage.findById(friendId);

        if (userOptional.isEmpty() || friendOptional.isEmpty()) {
            log.error("Пользователь с ID {}, либо друг с ID {} не найдены", userId, friendId);
            throw new NotFoundException("Один из пользователей с ID: " + userId + " или " + friendId + " не найден");
        }

        User user = userOptional.get();
        User friend = friendOptional.get();

        Set<Long> friendsOfUser = user.getFriends();
        friendsOfUser.retainAll(friend.getFriends());

        List<User> mutualList = friendsOfUser.stream()
                .map(mutualId -> userStorage.findById(mutualId)
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Общие друзья между пользователями {} и {}: {}", userId, friendId, friendsOfUser);

        return mutualList;
    }
}

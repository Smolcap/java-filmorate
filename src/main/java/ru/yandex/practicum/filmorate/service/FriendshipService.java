package ru.yandex.practicum.filmorate.service;

import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FriendshipService {

    private static Logger log = LoggerFactory.getLogger(FriendshipService.class);

    private final UserStorage userStorage;


    public FriendshipService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Set<Long> addFriends(@NotNull Long userId, @NotNull Long friendId) {
        log.info("Попытка добавить друга: пользователь ID: {}, друг ID: {}", userId, friendId);

        User user = userStorage.findById(userId);
        User userFriendId = userStorage.findById(friendId);

        if (user == null || userFriendId == null) {
            log.error("Пользователь с ID {} или друг с ID {} не найдены", userId, friendId);
            throw new NotFoundException("Пользователь  с ID: " + userId + " или друг с ID " + friendId + " не найдены");
        }

        if (user.getFriends().contains(friendId)) {
            log.error("Пользователь с ID {} уже является другом", friendId);
            throw new ValidationException("Пользователь уже является другом");
        }
        user.getFriends().add(friendId);
        userFriendId.getFriends().add(userId);
        userStorage.update(user);
        userStorage.update(userFriendId);

        log.info("Пользователь {} успешно добавил в друзья пользователя {}", userId, friendId);
        log.info("Список друзей пользователя {}: {}", user.getName(), user.getFriends());
        log.info("Список друзей пользователя {}: {}", userFriendId.getName(), userFriendId.getFriends());

        return user.getFriends();
    }

    public Set<Long> deleteFromFriends(@NotNull Long userId, @NotNull Long friendId) {
        User user = userStorage.findById(userId);
        User userFriendId = userStorage.findById(friendId);

        if (user == null || userFriendId == null) {
            log.error("Пользователь с ID {} или друг с ID {} не найдены", userId, friendId);
            throw new NotFoundException("Пользователь или друг с ID: " + userId + " " + friendId + " не найдены");
        }

        user.getFriends().remove(friendId);
        userFriendId.getFriends().remove(userId);

        userStorage.update(user);
        userStorage.update(userFriendId);

        log.info("Пользователь {} успешно удалил пользователя {} из своих друзей", userId, friendId);

        return user.getFriends();
    }

    public List<User> getAllFriends(@NotNull Long userId) {
        User user = userStorage.findById(userId);
        if (user == null) {
            log.error("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        log.info("Пользователь {} запросил список друзей: {}", userId, user.getFriends());

        Set<Long> friendsId = user.getFriends();
        List<User> friendList = friendsId.stream()
                .map(friendId -> userStorage.findById(friendId))
                .collect(Collectors.toList());
        return friendList;
    }

    public List<User> listMutualFriend(@NotNull Long userId, @NotNull Long friendId) {
        log.info("Попытка найти общих друзей между пользователями с ID: {} и {}", userId, friendId);

        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        if (user == null || friend == null) {
            log.error("Пользователь с ID {}, либо друг с ID {} не найдены", userId, friendId);
            throw new NotFoundException("Один из пользователей с ID: " + userId + " или " + friendId + " не найден");
        }

        Set<Long> friendsOfUser = user.getFriends();
        friendsOfUser.retainAll(friend.getFriends());
        List<User> mutualList = friendsOfUser.stream()
                .map(mutualId -> userStorage.findById(mutualId))
                .collect(Collectors.toList());

        log.info("Общие друзья между пользователями {} и {}: {}", userId, friendId, friendsOfUser);

        return mutualList;
    }
}

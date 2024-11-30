package ru.yandex.practicum.filmorate.storage.friend;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;

public interface FriendStorage {
    Set<Long> addFriends(Long userId, Long friendId);

    Set<Long> deleteFromFriends(Long userId, Long friendId);

    List<User> findAllFriend(Long userId);

    List<User> listMutualFriend(Long userId, Long friendId);
}

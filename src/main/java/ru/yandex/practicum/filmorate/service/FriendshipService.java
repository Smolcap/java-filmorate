package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.storage.friend.FriendStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FriendshipService {

    private final FriendStorage friendStorage;

    @Autowired
    public FriendshipService(@Qualifier("userDbStorage") FriendStorage friendStorage) {
        this.friendStorage = friendStorage;
    }

    public Set<Long> addFriends(Long userId, Long friendId) {
        return friendStorage.addFriends(userId, friendId);
    }

    public Set<Long> deleteFromFriends(Long userId, Long friendId) {
        return friendStorage.deleteFromFriends(userId, friendId);
    }

    public List<UserDto> getAllFriends(Long userId) {
        return friendStorage.findAllFriend(userId).stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> listMutualFriend(Long userId, Long friendId) {
        return friendStorage.listMutualFriend(userId, friendId).stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }
}

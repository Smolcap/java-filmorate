package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.service.FriendshipService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private static Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final FriendshipService friendshipService;


    public UserController(UserService userService, FriendshipService friendshipService) {
        this.userService = userService;
        this.friendshipService = friendshipService;
    }

    @GetMapping
    public List<UserDto> allListUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}/friends/common/{mutualFriendId}")
    public List<UserDto> getListMutualFriend(@PathVariable Long userId, @PathVariable Long mutualFriendId) {
        return friendshipService.listMutualFriend(userId, mutualFriendId);
    }

    @GetMapping("/{userId}/friends")
    public List<UserDto> getFriends(@PathVariable Long userId) {
        return friendshipService.getAllFriends(userId);
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto findById(@PathVariable("userId") Long userId) {
        return userService.findById(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody NewUserRequest userRequest) {
        return userService.createUser(userRequest);
    }

    @PutMapping
    public UserDto update(@Valid @RequestBody UpdateUserRequest updateUserRequest) {
        log.info("Данные для обновления пользователя с ID : {}", updateUserRequest);
        return userService.updateUser(updateUserRequest);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public Set<Long> addFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        return friendshipService.addFriends(userId, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public Set<Long> deleteFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        return friendshipService.deleteFromFriends(userId, friendId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable Long userId) {
        userService.deleteUserById(userId);
    }
}
package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
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
    public List<User> allListUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}/friends/common/{mutualFriendId}")
    public List<User> getListMutualFriend(@PathVariable Long userId, @PathVariable Long mutualFriendId) {
        return friendshipService.listMutualFriend(userId, mutualFriendId);
    }

    @GetMapping("/{userId}/friends")
    public List<User> getFriends(@PathVariable Long userId) {
        return friendshipService.getAllFriends(userId);
    }

    @GetMapping("/{userId}")
    public User findById(@PathVariable Long userId) {
        return userService.findById(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("Данные для обновления пользователя с ID : {}", newUser);
        return userService.updateUser(newUser);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public Set<Long> addFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        return friendshipService.addFriends(userId, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public Set<Long> deleteFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        return friendshipService.deleteFromFriends(userId, friendId);
    }
}
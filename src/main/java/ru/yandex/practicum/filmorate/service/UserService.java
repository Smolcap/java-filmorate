package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validaton.UserValidation;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public UserDto createUser(NewUserRequest request) {
        User user = UserMapper.mapToUser(request);

        UserValidation.validationForUser(user);

        user = userStorage.create(user);
        return UserMapper.mapToUserDto(user);
    }

    public UserDto updateUser(UpdateUserRequest request) {
        User user = UserMapper.mapToUser(request);
        UserValidation.validationForUser(user);
        user = userStorage.update(user);
        return UserMapper.mapToUserDto(user);
    }

    public List<UserDto> getAllUsers() {
        List<User> users = userStorage.getAllUsers();
        List<UserDto> userDto = users.stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());

        return userDto;
    }

    public UserDto findById(Long userId) {
        return userStorage.findById(userId)
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с ID: " + userId));
    }

    public void clearUser() {
        userStorage.clearUsers();
    }

    public void deleteUserById(Long userId) {
        userStorage.deleteUserById(userId);
    }
}

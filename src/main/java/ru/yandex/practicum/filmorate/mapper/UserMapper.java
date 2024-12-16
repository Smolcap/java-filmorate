package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {

    public static User mapToUser(NewUserRequest request) {
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .login(request.getLogin())
                .birthday(request.getBirthday())
                .friends(request.getFriends())
                .build();
        return user;
    }

    public static User mapToUser(UpdateUserRequest request) {
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .login(request.getLogin())
                .birthday(request.getBirthday())
                .friends(request.getFriends())
                .id(request.getId())
                .build();
        return user;
    }

    public static UserDto mapToUserDto(User user) {
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .login(user.getLogin())
                .email(user.getEmail())
                .birthday(user.getBirthday())
                .friends(user.getFriends())
                .build();

        return userDto;
    }

    public static User updateUserFields(User user, UpdateUserRequest request) {
        if (request.hasEmail()) {
            user.setEmail(request.getEmail());
        }
        if (request.hasLogin()) {
            user.setLogin(request.getLogin());
        }
        if (request.hasUsername()) {
            user.setName(request.getName());
        }
        if (request.hasBirthday()) {
            user.setBirthday(request.getBirthday());
        }
        if (request.hasFriends()) {
            user.setFriends(request.getFriends());
        }
        return user;
    }
}

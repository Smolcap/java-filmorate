package ru.yandex.practicum.filmorate.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Builder
@Data
public class UpdateUserRequest {
    Long id;
    String name;
    String email;
    String login;
    LocalDate birthday;

    @Builder.Default
    Set<Long> friends = new HashSet<>();

    public boolean hasUsername() {
        return name != null && !name.isEmpty();
    }

    public boolean hasEmail() {
        return email != null && !email.isEmpty();
    }

    public boolean hasLogin() {
        return login != null && !login.isEmpty();
    }

    public boolean hasBirthday() {
        return birthday != null;
    }

    public boolean hasFriends() {
        return friends != null && !friends.isEmpty();
    }
}

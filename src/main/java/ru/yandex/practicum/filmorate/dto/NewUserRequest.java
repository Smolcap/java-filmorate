package ru.yandex.practicum.filmorate.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Builder
@Data
public class NewUserRequest {
    String name;
    String email;
    String login;
    LocalDate birthday;

    @Builder.Default
    Set<Long> friends = new HashSet<>();
}

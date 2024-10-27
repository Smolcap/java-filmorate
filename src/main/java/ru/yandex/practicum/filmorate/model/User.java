package ru.yandex.practicum.filmorate.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Data
public class User {
    Long id;
    String email;
    String login;
    String name;
    LocalDate birthday;

    @Builder.Default
    Set<Long> friends = new HashSet<>();
}
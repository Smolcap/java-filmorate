package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class UserDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Long id;
    String name;
    String email;
    String login;
    LocalDate birthday;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Builder.Default
    Set<Long> friends = new HashSet<>();
}


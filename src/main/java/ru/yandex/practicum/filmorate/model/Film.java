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
public class Film {
    Long id;
    String name;
    String description;
    LocalDate releaseDate;
    Long duration;
    Mpa mpa;

    @Builder.Default
    Set<Long> userLikes = new HashSet<>();

    @Builder.Default
    Set<Genre> genres = new HashSet<>();

    int likes;
}

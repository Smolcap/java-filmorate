package ru.yandex.practicum.filmorate.dto;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.constants.MovieRating;
import ru.yandex.practicum.filmorate.model.Genre;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Builder
@Data
public class NewFilmRequest {
    String name;
    String description;
    LocalDate releaseDate;
    Long duration;
    MovieRating rating;
    Genre genre;

    @Builder.Default
    Set<Long> userLikes = new HashSet<>();

    int likes;
}

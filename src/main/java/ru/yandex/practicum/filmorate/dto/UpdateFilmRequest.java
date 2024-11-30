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
public class UpdateFilmRequest {
    private static final LocalDate BEGINNING_OF_THE_DATE = LocalDate.of(1895, 12, 28);

    String name;
    String description;
    LocalDate releaseDate;
    Long duration;
    MovieRating rating;
    Genre genre;

    @Builder.Default
    Set<Long> userLikes = new HashSet<>();

    int likes;

    public boolean hasName() {
        return name != null && !name.isEmpty();
    }

    public boolean hasDescription() {
        return description != null && description.length() < 200 && !description.isEmpty();
    }

    public boolean hasReleaseDate() {
        return releaseDate != null && releaseDate.isBefore(BEGINNING_OF_THE_DATE);
    }

    public boolean hasDuration() {
        return duration != null && !(duration <= 0);
    }

    public boolean hasRating() {
        return rating != null;
    }

    public boolean hasGenre() {
        return genre != null;
    }

    public boolean hasUserLikes() {
        return userLikes != null && !userLikes.isEmpty();
    }

    public boolean hasLikes() {
        return !(likes < 0);
    }
}

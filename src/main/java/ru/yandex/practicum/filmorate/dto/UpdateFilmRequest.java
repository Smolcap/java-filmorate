package ru.yandex.practicum.filmorate.dto;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Builder
@Data
public class UpdateFilmRequest {
    private static final LocalDate BEGINNING_OF_THE_DATE = LocalDate.of(1895, 12, 28);

    Long id;
    String name;
    String description;
    LocalDate releaseDate;
    Long duration;
    Mpa mpa;
    List<Genre> genres;

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
        return mpa != null;
    }

    public boolean hasGenre() {
        return genres != null && genres.isEmpty();
    }

    public boolean hasUserLikes() {
        return userLikes != null && !userLikes.isEmpty();
    }

    public boolean hasLikes() {
        return !(likes < 0);
    }
}

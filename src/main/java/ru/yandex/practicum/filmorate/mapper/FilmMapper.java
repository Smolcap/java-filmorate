package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmMapper {

    public static Film mapToFilm(NewFilmRequest request) {
        Film film = Film.builder()
                .name(request.getName())
                .description(request.getDescription())
                .releaseDate(request.getReleaseDate())
                .duration(request.getDuration())
                .genre(request.getGenre())
                .rating(request.getRating())
                .userLikes(request.getUserLikes())
                .likes(request.getLikes())
                .build();
        return film;
    }

    public static FilmDto mapToFilmDto(Film film) {
        FilmDto filmDto = FilmDto.builder()
                .id(film.getId())
                .name(film.getName())
                .releaseDate(film.getReleaseDate())
                .description(film.getDescription())
                .likes(film.getLikes())
                .duration(film.getDuration())
                .rating(film.getRating())
                .genre(film.getGenre())
                .build();
        return filmDto;
    }

    public static Film updateFilmFields(Film film, UpdateFilmRequest request) {
        if (request.hasName()) {
            film.setName(request.getName());
        }
        if (request.hasDescription()) {
            film.setDescription(request.getDescription());
        }
        if (request.hasReleaseDate()) {
            film.setReleaseDate(request.getReleaseDate());
        }
        if (request.hasDuration()) {
            film.setDuration(request.getDuration());
        }
        if (request.hasGenre()) {
            film.setGenre(request.getGenre());
        }
        if (request.hasLikes()) {
            film.setLikes(request.getLikes());
        }
        if (request.hasUserLikes()) {
            film.setUserLikes(request.getUserLikes());
        }
        if (request.hasRating()) {
            film.setRating(request.getRating());
        }
        return film;
    }
}

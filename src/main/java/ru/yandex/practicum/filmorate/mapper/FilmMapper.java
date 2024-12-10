package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashSet;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmMapper {

    public static Film mapToFilm(NewFilmRequest request) {
        Film film = Film.builder()
                .name(request.getName())
                .description(request.getDescription())
                .releaseDate(request.getReleaseDate())
                .duration(request.getDuration())
                .genres(request.getGenres())
                .mpa(request.getMpa())
                .likes(request.getLikes())
                .userLikes(request.getUserLikes())
                .build();

        if (film.getGenres() == null) {
            film.setGenres(new HashSet<>());
        }

        return film;
    }

    public static Film mapToFilm(UpdateFilmRequest request) {

        Film film = Film.builder()
                .name(request.getName())
                .description(request.getDescription())
                .releaseDate(request.getReleaseDate())
                .duration(request.getDuration())
                .genres(request.getGenres())
                .mpa(request.getMpa())
                .likes(request.getLikes())
                .userLikes(request.getUserLikes())
                .id(request.getId())
                .build();
        return film;
    }

    public static FilmDto mapToFilmDto(Film film) {
        FilmDto filmDto = FilmDto.builder()
                .id(film.getId())
                .name(film.getName())
                .releaseDate(film.getReleaseDate())
                .description(film.getDescription())
                .duration(film.getDuration())
                .likes(film.getLikes())
                .mpa(film.getMpa())
                .genres(film.getGenres())
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
            film.setGenres(request.getGenres());
        }
        if (request.hasLikes()) {
            film.setLikes(request.getLikes());
        }
        if (request.hasUserLikes()) {
            film.setUserLikes(request.getUserLikes());
        }
        if (request.hasRating()) {
            film.setMpa(request.getMpa());
        }
        return film;
    }
}

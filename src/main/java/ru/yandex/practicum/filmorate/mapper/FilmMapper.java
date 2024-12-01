package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.constants.MovieRating;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmMapper {

    public static Film mapToFilm(NewFilmRequest request) {
        MovieRating movieRating = MovieRating.fromId(request.getMpa().getId());

        Film film = Film.builder()
                .name(request.getName())
                .description(request.getDescription())
                .releaseDate(request.getReleaseDate())
                .duration(request.getDuration())
                .genres(request.getGenres())
                .mpa(movieRating)
                .userLikes(request.getUserLikes())
                .likes(request.getLikes())
                .build();
        return film;
    }

    public static MovieRating mapToIdRating(int mapId) {
        return MovieRating.fromId(mapId);
    }


    public static Film mapToFilm(UpdateFilmRequest request) {
        MovieRating movieRating = MovieRating.fromId(request.getMpa().getId());

        Film film = Film.builder()
                .name(request.getName())
                .description(request.getDescription())
                .releaseDate(request.getReleaseDate())
                .duration(request.getDuration())
                .genres(request.getGenres())
                .mpa(movieRating)
                .userLikes(request.getUserLikes())
                .likes(request.getLikes())
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
                .likes(film.getLikes())
                .duration(film.getDuration())
                .mpa(new Mpa((film.getMpa().getId())))
                .genres(film.getGenres())
                .build();
        return filmDto;
    }

    public static MpaDto mapToFilmDto(MovieRating movieRating) {
        return new MpaDto(movieRating.getId(), getRatingName(movieRating));
    }

    public static String getRatingName(MovieRating rating) {
        switch (rating) {
            case G:
                return "G";
            case PG:
                return "PG";
            case PG_13:
                return "PG-13";
            case R:
                return "R";
            case NC_17:
                return "NC-17";
            default:
                throw new NotFoundException("Некорректный рейтинг: " + rating);
        }
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
            film.setMpa(mapToIdRating(request.getMpa().getId()));
        }
        return film;
    }
}

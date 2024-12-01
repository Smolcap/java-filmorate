package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.constants.MovieRating;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film newFilm);

    List<Film> getAllFilms();

    void clearFilm();

    Optional<Film> findById(Long filmId);

    boolean deleteFilmById(Long filmId);

    MovieRating getRatingNameById(int mpaId);

    List<MovieRating> getAllRating();
}

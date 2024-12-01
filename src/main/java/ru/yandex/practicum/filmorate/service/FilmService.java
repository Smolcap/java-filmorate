package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.constants.MovieRating;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.validaton.FilmValidation;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public FilmDto createFilm(NewFilmRequest request) {
        Film film = FilmMapper.mapToFilm(request);
        FilmValidation.validationRating(film.getMpa().getId());
        FilmValidation.validationForFilm(film);

        film = filmStorage.create(film);
        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto updateFilm(UpdateFilmRequest request) {
        Film film = FilmMapper.mapToFilm(request);
        FilmValidation.validationForFilm(film);
        film = filmStorage.update(film);
        return FilmMapper.mapToFilmDto(film);
    }

    public List<FilmDto> getAllFilms() {
        List<Film> films = filmStorage.getAllFilms();
        List<FilmDto> filmDto = films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());

        return filmDto;
    }

    public MpaDto getRatingNameById(int mpaId) {
        FilmValidation.validationRating(mpaId);
        MovieRating movieRating = filmStorage.getRatingNameById(mpaId);
        return FilmMapper.mapToFilmDto(movieRating);
    }

    public List<MpaDto> getAllRating() {
        List<MovieRating> rating = filmStorage.getAllRating();
        List<MpaDto> mpaDto = rating.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
        return mpaDto;
    }

    public void clearFilm() {
        filmStorage.clearFilm();
    }

    public FilmDto findById(Long filmId) {
        return filmStorage.findById(filmId)
                .map(FilmMapper::mapToFilmDto)
                .orElseThrow(() -> new NotFoundException("Фильм не найден с ID: " + filmId));
    }

    public void deleteFilmById(Long filmId) {
        filmStorage.deleteFilmById(filmId);
    }
}

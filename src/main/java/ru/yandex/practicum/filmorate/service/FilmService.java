package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    private static Logger log = LoggerFactory.getLogger(FilmService.class);

    private static final LocalDate BEGINNING_OF_THE_DATE = LocalDate.of(1895, 12, 28);

    public FilmDto createFilm(NewFilmRequest request) {
        if (request.getName() == null || request.getName().isEmpty()) {
            log.warn("Наименование фильма {}", request.getName());
            throw new ValidationException("Имя не должно быть пустым");
        }
        if (request.getDescription().length() >= 200) {
            log.warn("Описание фильма {}", request.getDescription());
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (request.getReleaseDate().isBefore(BEGINNING_OF_THE_DATE)) {
            log.warn("Дата релиза фильма {}", request.getReleaseDate());
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (request.getDuration() == null || request.getDuration() <= 0) {
            log.warn("Продолжительность фильма {}", request.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        Film film = FilmMapper.mapToFilm(request);
        film = filmStorage.create(film);
        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto updateFilm(Long filmId, UpdateFilmRequest request) {
        Film updateFilm = filmStorage.findById(filmId)
                .map(film -> FilmMapper.updateFilmFields(film, request))
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));
        updateFilm = filmStorage.update(updateFilm);
        return FilmMapper.mapToFilmDto(updateFilm);

    }

    public List<FilmDto> getAllFilms() {
        List<Film> films = filmStorage.getAllFilms();
        List<FilmDto> filmDto = films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());

        return filmDto;
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

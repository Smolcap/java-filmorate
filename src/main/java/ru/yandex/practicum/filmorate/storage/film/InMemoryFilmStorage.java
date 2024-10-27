package ru.yandex.practicum.filmorate.storage.film;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private static final LocalDate BEGINNING_OF_THE_DATE = LocalDate.of(1895, 12, 28);

    private static Logger log = LoggerFactory.getLogger(InMemoryFilmStorage.class);

    private Map<Long, Film> films = new HashMap<>();

    @Override
    public Film create(Film film) {
        log.info("Начало создание фильма");

        validationForFilms(film);
        film.setId(generationId());
        film.setUserLikes(new HashSet<>());

        log.debug("Фильм создан {}", film);
        films.put(film.getId(), film);
        log.debug("Фильм добавлен в хранилище {}", film.getId());

        return film;
    }

    @Override
    public Film update(Film newFilm) {
        log.info("Начало обновление фильма");

        validationUpdate(newFilm);
        validationUpdate(newFilm);

        log.debug("Обновление фильма с Id: {}", newFilm.getId());

        films.put(newFilm.getId(), newFilm);
        log.debug("Фильм обновлён с Id {}", newFilm.getId());
        return newFilm;
    }


    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public void clearFilm() {
        films.clear();
    }

    @Override
    public Film findById(Long filmId) {
        Film film = films.get(filmId);

        if (film == null) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
        return film;
    }

    private void validationForFilms(Film film) {
        if (film.getName() == null || film.getName().isEmpty()) {
            log.warn("Наименование фильма {}", film.getName());
            throw new ValidationException("Имя не должно быть пустым");
        }
        if (film.getDescription().length() >= 200) {
            log.warn("Описание фильма {}", film.getDescription());
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate().isBefore(BEGINNING_OF_THE_DATE)) {
            log.warn("Дата релиза фильма {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.warn("Продолжительность фильма {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    private void validationUpdate(Film newFilm) {
        if (newFilm.getId() == null) {
            log.debug("Id не указан для обновления");
            throw new ValidationException("Id должен быть указан");
        }
        if (!films.containsKey(newFilm.getId())) {
            log.warn(" Фильм не найден с Id {}", newFilm.getId());
            throw new NotFoundException("Фильм не найден");
        }
    }

    private long generationId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}

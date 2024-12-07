package ru.yandex.practicum.filmorate.storage.Like;

import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.LikeService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Component
public class InMemoryLikeStorage implements LikeStorage {
    private static Logger log = LoggerFactory.getLogger(LikeService.class);
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public InMemoryLikeStorage(@Qualifier("inMemoryFilmStorage") FilmStorage filmStorage, @Qualifier("inMemoryUserStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    @Override
    public Set<Long> addLike(@NotNull Long filmId, @NotNull Long userId) {
        log.debug("Добавление лайка пользователем {} к фильму {}", userId, filmId);

        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> {
                    log.error("Фильм с ID {} не найден", filmId);
                    return new ValidationException("Фильм не найден");
                });

        if (userStorage.findById(userId) == null) {
            log.error("Пользователь с ID {} не существует, не может поставить лайк", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не существует");
        }

        boolean isAdded = film.getUserLikes().add(userId);
        if (!isAdded) {
            log.warn("Пользователь {} уже поставил лайк фильму {}", userId, filmId);
            throw new ValidationException("Пользователь может поставить только один лайк фильму");
        }

        log.info("Пользователь {} успешно поставил лайк фильму {}", userId, filmId);

        return film.getUserLikes();
    }

    @Override
    public Set<Long> deleteLike(@NotNull Long filmId, @NotNull Long userId) {
        log.debug("Удаление лайка пользователем {} к фильму {}", userId, filmId);

        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> {
                    log.warn("Фильм с ID {} не существует", filmId);
                    return new NotFoundException("Фильм с ID " + filmId + " не существует");
                });

        if (userStorage.findById(userId) == null) {
            log.error("Пользователь с ID {} не существует", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не существует");
        }

        boolean isRemoved = film.getUserLikes().remove(userId);
        if (!isRemoved) {
            log.warn("Пользователь {} не ставил лайк фильму {}", userId, filmId);
            throw new ValidationException("Пользователь не ставил лайк фильму");
        }

        filmStorage.update(film);
        log.debug("Пользователь {} успешно удалил лайк у фильма {}", userId, filmId);

        return film.getUserLikes();
    }

    @Override
    public List<Film> top10PopularMovies(Integer count) {
        log.debug("Количество фильмов для создания списка {}", count);

        List<Film> films = filmStorage.getAllFilms();
        log.debug("Список фильмов для сортировки {}", films);

        List<Film> topFilms = new ArrayList<>();

        int topCount;
        if (count != null && count > 0) {
            topCount = Math.min(count, films.size());
        } else {
            topCount = Math.min(10, films.size());
        }

        films.sort(new Comparator<Film>() {
            @Override
            public int compare(Film f1, Film f2) {
                return Integer.compare(f2.getUserLikes().size(), f1.getUserLikes().size());
            }
        });

        for (int i = 0; i < topCount; i++) {
            topFilms.add(films.get(i));
        }
        return topFilms;
    }
}

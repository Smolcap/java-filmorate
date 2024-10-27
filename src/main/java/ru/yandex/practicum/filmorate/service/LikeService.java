package ru.yandex.practicum.filmorate.service;

import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class LikeService {
    private static Logger log = LoggerFactory.getLogger(LikeService.class);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public LikeService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Set<Long> addLike(@NotNull Long filmId, @NotNull Long userId) {
        log.debug("Добавление лайка пользователем {} к фильму {}", userId, filmId);

        Film filmForLikeAdd = filmStorage.findById(filmId);
        if (filmForLikeAdd == null) {
            log.error("Фильм с ID {} не найден", filmId);
            throw new ValidationException("Фильм не найден");
        }

        if (userStorage.findById(userId) == null) {
            log.error("Пользователь с ID {}, который ставит лайк не существует", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " ,котоырй хочет поставить like не существует");
        }

        if (!filmForLikeAdd.getUserLikes().add(userId)) {
            log.warn("Пользователь {} уже поставил лайк фильму {}", userId, filmId);
            throw new ValidationException("Пользователь может поставить только один раз лайк фильму");
        }

        filmForLikeAdd.setLikes(filmForLikeAdd.getLikes() + 1);

        filmStorage.update(filmForLikeAdd);

        log.info("Пользователь {} успешно поставил лайк фильму {}", userId, filmId);

        return filmForLikeAdd.getUserLikes();
    }

    public Set<Long> deleteLike(@NotNull Long filmId, @NotNull Long userId) {
        log.debug("Удаление лайка пользователем {} к фильму {}", userId, filmId);
        Film filmForLikeDelete = filmStorage.findById(filmId);

        if (filmForLikeDelete == null) {
            log.warn("Фильм с ID {} не существует", filmId);
            throw new NotFoundException("Фильма с ID " + filmId + " не существует");
        }

        if (userStorage.findById(userId) == null || !filmForLikeDelete.getUserLikes().remove(userId)) {
            log.error("Пользователь с ID {}, который удаляет лайк не существует", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " ,котоырй хочет поставить like не существует");
        }

        filmForLikeDelete.setLikes(filmForLikeDelete.getLikes() - 1);

        filmStorage.update(filmForLikeDelete);
        log.debug("Пользователь {} успешно удалил лайк у фильма {}", userId, filmId);

        return filmForLikeDelete.getUserLikes();
    }

    public List<Film> top10PopularMovies(Integer count) {
        log.debug("Количество фильмов для создания списка {}", count);

        List<Film> films = filmStorage.getAllFilms();
        log.debug("Список фильмов для сортировки {}", films);

        return films.stream()
                .sorted(Comparator.comparingInt(Film::getLikes).reversed())
                .limit(count)
                .toList();
    }
}

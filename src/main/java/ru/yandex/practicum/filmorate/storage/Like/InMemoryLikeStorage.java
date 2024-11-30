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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

        Optional<Film> filmOptional = filmStorage.findById(filmId);
        if (filmOptional.isEmpty()) {
            log.error("Фильм с ID {} не найден", filmId);
            throw new ValidationException("Фильм не найден");
        }

        Film filmForLikeAdd = filmOptional.get();

        if (userStorage.findById(userId) == null) {
            log.error("Пользователь с ID {}, который ставит лайк не существует", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " ,который хочет поставить лайк, не существует");
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

    @Override
    public Set<Long> deleteLike(@NotNull Long filmId, @NotNull Long userId) {
        log.debug("Удаление лайка пользователем {} к фильму {}", userId, filmId);

        Optional<Film> filmOptional = filmStorage.findById(filmId);
        if (filmOptional.isEmpty()) {
            log.warn("Фильм с ID {} не существует", filmId);
            throw new NotFoundException("Фильм с ID " + filmId + " не существует");
        }

        Film filmForLikeDelete = filmOptional.get();

        if (userStorage.findById(userId) == null || !filmForLikeDelete.getUserLikes().remove(userId)) {
            log.error("Пользователь с ID {}, который удаляет лайк, не существует", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " ,который хочет удалить лайк, не существует");
        }

        filmForLikeDelete.setLikes(filmForLikeDelete.getLikes() - 1);
        filmStorage.update(filmForLikeDelete);

        log.debug("Пользователь {} успешно удалил лайк у фильма {}", userId, filmId);
        return filmForLikeDelete.getUserLikes();
    }

    @Override
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

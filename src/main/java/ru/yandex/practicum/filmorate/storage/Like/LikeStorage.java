package ru.yandex.practicum.filmorate.storage.Like;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Set;

public interface LikeStorage {
    Set<Long> addLike(Long filmId, Long userId);

    Set<Long> deleteLike(Long filmId, Long userId);

    List<Film> top10PopularMovies(Integer count);
}

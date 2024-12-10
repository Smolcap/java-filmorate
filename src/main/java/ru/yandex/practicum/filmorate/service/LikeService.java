package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.storage.Like.LikeStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LikeService {

    LikeStorage likeStorage;

    @Autowired
    public LikeService(@Qualifier("filmDbStorage") LikeStorage likeStorage) {
        this.likeStorage = likeStorage;
    }

    public Set<Long> addLike(Long filmId, Long userId) {
        return likeStorage.addLike(filmId, userId);
    }

    public Set<Long> deleteLike(Long filmId, Long userId) {
        return likeStorage.deleteLike(filmId, userId);
    }

    public List<FilmDto> top10PopularMovies(Integer count) {
        return likeStorage.top10PopularMovies(count).stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }
}

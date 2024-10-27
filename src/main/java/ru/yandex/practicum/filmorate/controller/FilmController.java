package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.LikeService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;
    private final LikeService likeService;

    public FilmController(FilmService filmService, LikeService likeService) {
        this.filmService = filmService;
        this.likeService = likeService;
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public Set<Long> deleteLike(@PathVariable Long filmId, @PathVariable Long userId) {
        return likeService.deleteLike(filmId, userId);
    }

    @GetMapping("/popular")
    public List<Film> topFilms(@RequestParam(defaultValue = "10") Integer count) {
        return likeService.top10PopularMovies(count);
    }

    @GetMapping
    public List<Film> allListFilm() {
        return filmService.getAllFilms();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        return filmService.updateFilm(newFilm);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public Set<Long> addLikeFilms(@PathVariable Long filmId, @PathVariable Long userId) {
        return likeService.addLike(filmId, userId);
    }
}

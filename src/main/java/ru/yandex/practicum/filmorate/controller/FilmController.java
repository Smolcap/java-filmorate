package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
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

    @GetMapping("/popular")
    public List<FilmDto> topFilms(@RequestParam(defaultValue = "10") Integer count) {
        return likeService.top10PopularMovies(count);
    }

    @GetMapping
    public List<FilmDto> allListFilm() {
        return filmService.getAllFilms();
    }

    @GetMapping("/{filmId}")
    @ResponseStatus(HttpStatus.OK)
    public FilmDto findById(@PathVariable("filmId") Long filmId) {
        return filmService.findById(filmId);
    }

    @PostMapping
    public FilmDto create(@Valid @RequestBody NewFilmRequest filmRequest) {
        return filmService.createFilm(filmRequest);
    }

    @PutMapping("/{filmId}")
    public FilmDto update(@Valid @RequestBody @PathVariable("filmId") Long filmId, @RequestBody UpdateFilmRequest updateFilmRequest) {
        return filmService.updateFilm(filmId, updateFilmRequest);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public Set<Long> addLikeFilms(@PathVariable Long filmId, @PathVariable Long userId) {
        return likeService.addLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public Set<Long> deleteLike(@PathVariable Long filmId, @PathVariable Long userId) {
        return likeService.deleteLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilmById(@PathVariable Long filmId) {
        filmService.deleteFilmById(filmId);
    }
}

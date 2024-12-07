package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.RatingService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MpaController {
    private final RatingService ratingService;

    public MpaController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @GetMapping("/{mpaId}")
    public Mpa getRatingNameById(@PathVariable int mpaId) {
        return ratingService.getRatingNameById(mpaId);
    }

    @GetMapping
    public List<Mpa> getAllRating() {
        return ratingService.getAllRating();
    }
}

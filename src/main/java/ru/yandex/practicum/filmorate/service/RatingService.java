package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.RatingStorage;

import java.util.List;

@Service
public class RatingService {
    private final RatingStorage ratingStorage;

    @Autowired
    public RatingService(RatingStorage ratingStorage) {
        this.ratingStorage = ratingStorage;
    }

    public Mpa getRatingNameById(int mpaId) {
        return ratingStorage.getRatingById(mpaId)
                .orElseThrow(() -> new NotFoundException("Рейтинг с ID " + mpaId + " не найден"));
    }

    public List<Mpa> getAllRating() {
        return ratingStorage.getAllRatings();
    }
}

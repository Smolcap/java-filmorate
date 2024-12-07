package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

public interface RatingStorage {
    List<Mpa> getAllRatings();

    Optional<Mpa> getRatingById(int id);
}

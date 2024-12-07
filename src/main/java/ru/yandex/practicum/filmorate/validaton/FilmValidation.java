package ru.yandex.practicum.filmorate.validaton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;


public class FilmValidation {
    private static final LocalDate BEGINNING_OF_THE_DATE = LocalDate.of(1895, 12, 28);
    private static Logger log = LoggerFactory.getLogger(FilmValidation.class);
    private static final Integer MAX_NUMBER_RATING = 5;
    private static final Integer MIN_NUMBER_RATING = 0;

    public static void validationForFilm(Film film) {
        if (film.getName() == null || film.getName().isEmpty()) {
            log.warn("Наименование фильма {}", film.getName());
            throw new ValidationException("Имя не должно быть пустым");
        }
        if (film.getDescription().length() >= 200) {
            log.warn("Описание фильма {}", film.getDescription());
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate().isBefore(BEGINNING_OF_THE_DATE)) {
            log.warn("Дата релиза фильма {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.warn("Продолжительность фильма {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        if (film.getMpa().getId() > MAX_NUMBER_RATING || film.getMpa().getId() <= MIN_NUMBER_RATING) {
            throw new ValidationException("Некорректный идентификатор рейтинга: " + film.getMpa().getId());
        }
    }
}

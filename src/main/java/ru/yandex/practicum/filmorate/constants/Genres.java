package ru.yandex.practicum.filmorate.constants;

import ru.yandex.practicum.filmorate.exception.NotFoundException;

public enum Genres {
    COMEDY(1, "Комедия"),
    DRAMA(2, "Драма"),
    CARTOON(3, "Мультфильм"),
    THRILLER(4, "Триллер"),
    DOCUMENTARY(5, "Документальный"),
    ACTION(6, "Боевик");

    private final long id;
    private final String name;

    Genres(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static Genres fromId(long id) {
        for (Genres genre : Genres.values()) {
            if (genre.getId() == id) {
                return genre;
            }
        }
        throw new NotFoundException("Некорректный идентификатор жанра: " + id);
    }
}

package ru.yandex.practicum.filmorate.constants;


import ru.yandex.practicum.filmorate.exception.NotFoundException;

public enum MovieRating {
    G(1),
    PG(2),
    PG_13(3),
    R(4),
    NC_17(5);

    private final int id;

    MovieRating(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static MovieRating fromId(int id) {
        for (MovieRating rating : MovieRating.values()) {
            if (rating.getId() == id) {
                return rating;
            }
        }
        throw new NotFoundException("Некорректный идентификатор рейтинга: " + id);
    }
}

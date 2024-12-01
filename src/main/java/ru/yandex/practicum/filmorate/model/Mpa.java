package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.constants.MovieRating;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class Mpa {
    int id;
    String name;

    @JsonCreator
    public Mpa(int id) {
        this.id = id;
        this.name = MovieRating.fromId(id).name();
    }
}

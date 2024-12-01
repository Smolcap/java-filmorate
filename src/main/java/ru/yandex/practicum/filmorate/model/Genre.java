package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.constants.Genres;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class Genre {
    Long id;
    String name;

    @JsonCreator
    public Genre(Long id, String name) {
        this.id = id;
        this.name = Genres.fromId(id).name();
    }
}

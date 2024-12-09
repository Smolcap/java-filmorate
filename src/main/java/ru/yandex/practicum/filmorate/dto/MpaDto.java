package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
public class MpaDto {
    int id;
    String name;

    @JsonCreator
    public MpaDto(int id, String name) {
        this.id = id;
        this.name = name;
    }
}

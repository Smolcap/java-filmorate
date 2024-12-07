package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.BaseRepository;
import ru.yandex.practicum.filmorate.dao.mapping.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
public class GenreDbStorage extends BaseRepository<Genre> implements GenreStorage {
    private static final String FIND_ALL_GENRE_QUERY = "SELECT * FROM genre";
    private static final String FIND_GENRE_BY_ID = "SELECT * FROM genre WHERE genre_id = ?";

    public GenreDbStorage(JdbcTemplate jdbc, GenreRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Genre> getAllGenres() {
        return findMany(FIND_ALL_GENRE_QUERY);
    }

    @Override
    public Optional<Genre> getGenreById(Long genreId) {
        return findOne(FIND_GENRE_BY_ID, genreId);
    }
}

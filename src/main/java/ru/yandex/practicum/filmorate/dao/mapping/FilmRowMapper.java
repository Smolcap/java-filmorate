package ru.yandex.practicum.filmorate.dao.mapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.RatingStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    private final JdbcTemplate jdbcTemplate;
    private final RatingStorage ratingStorage;

    private static final Logger logger = LoggerFactory.getLogger(FilmRowMapper.class);

    public FilmRowMapper(JdbcTemplate jdbcTemplate, RatingStorage ratingStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.ratingStorage = ratingStorage;
    }

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Long filmId = resultSet.getLong("film_id");
        int mpaRatingId = resultSet.getInt("rating_id");

        Mpa getMpa = ratingStorage.getRatingById(mpaRatingId)
                .orElseThrow(() -> new ValidationException("Рейтинг с ID " + mpaRatingId + " не найден"));

        Film film = Film.builder()
                .id(filmId)
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .duration(resultSet.getLong("duration"))
                .genres(getGenresForFilm(filmId))
                .mpa(getMpa)
                .userLikes(getUserLikesForFilm(filmId))
                .build();
        return film;
    }

    private Set<Genre> getGenresForFilm(Long filmId) {
        String findGenres = "SELECT g.genre_id, g.name FROM film_genre fg JOIN genre g ON fg.genre_id = g.genre_id WHERE fg.film_id = ?";
        List<Genre> genres = jdbcTemplate.query(findGenres, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getLong("genre_id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, filmId);
        return new HashSet<>(genres);
    }

    private Set<Long> getUserLikesForFilm(Long filmId) {
        String findUserLike = "SELECT user_id FROM like_user WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(findUserLike, new Object[]{filmId}, Long.class));
    }
}

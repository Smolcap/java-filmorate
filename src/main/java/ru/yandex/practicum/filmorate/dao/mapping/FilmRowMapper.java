package ru.yandex.practicum.filmorate.dao.mapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.constants.MovieRating;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    private final JdbcTemplate jdbcTemplate;

    private static final Logger logger = LoggerFactory.getLogger(FilmRowMapper.class);

    public FilmRowMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Long filmId = resultSet.getLong("film_id");
        int ratingId = resultSet.getInt("rating_id");
        MovieRating mpaRating = MovieRating.fromId(ratingId);

        Film film = Film.builder()
                .id(filmId)
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .duration(resultSet.getLong("duration"))
                .likes(resultSet.getInt("like_film"))
                .genres(getGenresForFilm(filmId))
                .mpa(mpaRating)
                .userLikes(getUserLikesForFilm(filmId))
                .build();
        return film;
    }

    private List<Genre> getGenresForFilm(Long filmId) {
        String findGenres = "SELECT g.genre_id, g.name FROM film_genre fg JOIN genre g ON fg.genre_id = g.genre_id WHERE fg.film_id = ?";
        return jdbcTemplate.query(findGenres, new Object[]{filmId}, (rs, rowNum) ->
                new Genre(rs.getLong("genre_id"), rs.getString("name"))
        );
    }

    private Set<Long> getUserLikesForFilm(Long filmId) {
        String findUserLike = "SELECT user_id FROM like_user WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(findUserLike, new Object[]{filmId}, Long.class));
    }
}

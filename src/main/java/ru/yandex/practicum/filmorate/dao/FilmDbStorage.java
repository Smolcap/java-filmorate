package ru.yandex.practicum.filmorate.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mapping.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.Like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository("filmDbStorage")
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage, LikeStorage {
    private static final String INSERT_FILM = "INSERT INTO film (name, description, release_date, duration, like_film," +
            " genre_id, rating) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM = "UPDATE film SET name = ?, description = ?, release_date = ?, duration = ?" +
            ", like_film = ?, genre_id = ?, rating = ? WHERE film_id = ?";
    private static final String FIND_ALL_FILM_QUERY = "SELECT * FROM film";
    private static final String FIND_FILM_BY_ID_QUERY = "SELECT * FROM film WHERE film_id = ?";
    private static final String INSERT_GENRE_QUERY = "INSERT INTO genre (genre_id, name) VALUES (?, ?)";
    private static final String DELETE_FILM_BY_ID_QUERY = "DELETE FROM film WHERE film_id = ?";
    private static final String DELETE_FILMS_QUERY = "DELETE FROM film";
    private static final String FIND_COUNT_LIKE_ON_FILM = "SELECT COUNT(*) FROM like_user" +
            " WHERE user_id = ? AND film_id = ?";
    private static final String INSERT_LIKE_FILM = "INSERT INTO like_user (user_id, film_id) VALUES (?, ?)";
    private static final String UPDATE_ADD_LIKE_FILM_QUERY = "UPDATE film SET like_film = like_film + 1 WHERE film_id = ?";
    private static final String DELETE_LIKE_FILM_QUERY = "DELETE FROM like_user WHERE user_id = ? AND film_id = ?";
    private static final String UPDATE_REMOVE_LIKE_QUERY = "UPDATE film SET like_film = like_film - 1 WHERE film_id = ?";
    private static final String FIND_TOP_10_POPULAR_MOVIES = "SELECT * FROM film ORDER BY like_film DESC LIMIT 10";


    private static final Logger logger = LoggerFactory.getLogger(FilmDbStorage.class);

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Film create(Film film) {
        logger.info("Создание фильма: {}", film);
        Long id = insert(
                INSERT_FILM,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getLikes(),
                film.getGenre().getId(),
                film.getRating().name()
        );
        logger.info("Фильм успешно создан с ID: {}", id);
        film.setId(id);
        Genre genre = getGenreById(film.getGenre().getId());
        film.setGenre(genre);
        logger.info("Текущий жанр фильма {}", film.getGenre());
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        update(
                UPDATE_FILM,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                newFilm.getLikes(),
                newFilm.getGenre().getId(),
                newFilm.getRating().name(),
                newFilm.getId()
        );
        return newFilm;
    }

    @Override
    public List<Film> getAllFilms() {
        return findMany(FIND_ALL_FILM_QUERY);
    }

    @Override
    public Optional<Film> findById(Long filmId) {
        return findOne(FIND_FILM_BY_ID_QUERY, filmId);
    }

    @Override
    public boolean deleteFilmById(Long filmId) {
        return delete(DELETE_FILM_BY_ID_QUERY, filmId);
    }

    @Override
    public void clearFilm() {
        update(DELETE_FILMS_QUERY);
    }

    @Override
    public Set<Long> addLike(Long filmId, Long userId) {
        if (!isLikeExists(userId, filmId)) {
            insertAddLike(INSERT_LIKE_FILM, userId, filmId);
            update(UPDATE_ADD_LIKE_FILM_QUERY, filmId);
        }
        return getLikesForFilm(filmId);
    }

    @Override
    public Set<Long> deleteLike(Long filmId, Long userId) {
        if (isLikeExists(filmId, userId)) {
            update(DELETE_LIKE_FILM_QUERY, filmId, userId);
            update(UPDATE_REMOVE_LIKE_QUERY, filmId);
        }
        return getLikesForFilm(filmId);
    }

    @Override
    public List<Film> top10PopularMovies(Integer count) {
        return findMany(FIND_TOP_10_POPULAR_MOVIES);
    }

    private Genre getGenreById(Long genreId) {
        String query = "SELECT * FROM genre WHERE genre_id = ?";
        return jdbc.queryForObject(query, new Object[]{genreId}, (rs, rowNum) ->
                new Genre(rs.getLong("genre_id"), rs.getString("name"))
        );
    }

    private Set<Long> getLikesForFilm(Long filmId) {
        return new HashSet<>(jdbc.queryForList(
                "SELECT user_id FROM like_user WHERE film_id = ?",
                Long.class,
                filmId
        ));
    }

    private boolean isLikeExists(Long filmId, Long userId) {
        return count(FIND_COUNT_LIKE_ON_FILM, filmId, userId) > 0;
    }
}

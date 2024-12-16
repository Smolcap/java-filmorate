package ru.yandex.practicum.filmorate.storage.film;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.BaseRepository;
import ru.yandex.practicum.filmorate.dao.mapping.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.Like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository("filmDbStorage")
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage, LikeStorage {
    private static final String INSERT_FILM = "INSERT INTO film (name, description, release_date, duration, rating_id," +
            " like_film) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM = "UPDATE film SET name = ?, description = ?, release_date = ?, " +
            "duration = ?, rating_id = ?, like_film = ? WHERE film_id = ?";
    private static final String DELETE_GENRE_QUERY = "DELETE FROM film_genre WHERE film_id = ?";
    private static final String FIND_ALL_FILM_QUERY = "SELECT * FROM film";
    private static final String FIND_FILM_BY_ID_QUERY = "SELECT * FROM film WHERE film_id = ?";
    private static final String INSERT_GENRE_QUERY = "INSERT INTO genre (name) VALUES (?)";
    private static final String DELETE_FILM_BY_ID_QUERY = "DELETE FROM film WHERE film_id = ?";
    private static final String DELETE_FILMS_QUERY = "DELETE FROM film";
    private static final String FIND_COUNT_LIKE_ON_FILM = "SELECT COUNT(*) FROM like_user" +
            " WHERE user_id = ? AND film_id = ?";
    private static final String INSERT_LIKE_FILM = "INSERT INTO like_user (user_id, film_id) VALUES (?, ?)";
    private static final String UPDATE_ADD_LIKE_FILM_QUERY = "UPDATE film SET like_film = like_film + 1 WHERE film_id = ?";
    private static final String DELETE_LIKE_FILM = "DELETE FROM like_user WHERE user_id = ? AND film_id = ?";
    private static final String UPDATE_REMOVE_LIKE_QUERY = "UPDATE film SET like_film = like_film - 1 WHERE film_id = ?";
    private static final String FIND_TOP_10_POPULAR_MOVIES = "SELECT * FROM film ORDER BY like_film DESC LIMIT ?";
    private static final String INSERT_GENRE = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    public static final String FIND_RATING_QUERY = "SELECT * FROM rating WHERE rating_id = ?";
    private static final String INSERT_RATING_QUERY = "INSERT INTO rating (rating_id, name) VALUES (?, ?)";
    private static final String CHECK_RATING_EXISTS = "SELECT COUNT(*) FROM rating WHERE rating_id = ?";
    private static final String FIND_ALL_RATING_QUERY = "SELECT * FROM rating";
    private static final String DELETE_LIKE_FILM_QUERY = "DELETE FROM like_user WHERE film_id = ?";
    private static final String DELETE_FILM_USER_QUERY = "DELETE FROM film_user WHERE film_id = ?";


    private static final Logger logger = LoggerFactory.getLogger(FilmDbStorage.class);

    GenreDbStorage genreDbStorage;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper, GenreDbStorage genreDbStorage) {
        super(jdbc, mapper);
        this.genreDbStorage = genreDbStorage;
    }

    @Override
    public Film create(Film film) {
        logger.info("Создание фильма: {}", film);
        Long id = addFilm(film);
        film.setId(id);
        addFilmGenres(film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        logger.info("Обновление фильма с ID: {}, Имя: {}, Описание: {}", newFilm.getId(), newFilm.getName(),
                newFilm.getDescription());

        Optional<Film> existing = findById(newFilm.getId());
        if (existing.isEmpty()) {
            logger.warn("Фильм не найден с ID: {}", newFilm.getId());
            throw new NotFoundException("Фильм с таким Id не найден");
        }
        removeGenre(newFilm.getId());
        updateFilm(newFilm);
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
        logger.info("Удаление жанров для фильма с ID: {}", filmId);
        int deletedGenresCount = jdbc.update("DELETE FROM film_genre WHERE film_id = ?", filmId);
        logger.info("Удалено {} жанров для фильма с ID: {}", deletedGenresCount, filmId);


        logger.info("Удаление лайков для фильма с ID: {}", filmId);
        int deletedLikesCount = jdbc.update("DELETE FROM like_user WHERE film_id = ?", filmId);
        logger.info("Удалено {} лайков для фильма с ID: {}", deletedLikesCount, filmId);

        logger.info("Удаление фильма с ID: {}", filmId);
        int deletedFilmCount = jdbc.update("DELETE FROM film WHERE film_id = ?", filmId);
        logger.info("Удален фильм с ID: {}, количество затронутых строк: {}", filmId, deletedFilmCount);

        return deletedFilmCount > 0;
    }

    @Override
    public void clearFilm() {
        logger.info("Удаление всех жанров из базы данных");
        jdbc.update("DELETE FROM film_genre");

        logger.info("Удаление всех лайков из базы данных");
        jdbc.update("DELETE FROM like_user");

        logger.info("Удаление всех фильмов из базы данных");
        jdbc.update("DELETE FROM film");
    }

    @Override
    public Set<Long> addLike(Long userId, Long filmId) {
        if (!isLikeExists(userId, filmId)) {
            insertAddLike(INSERT_LIKE_FILM, userId, filmId);
            update(UPDATE_ADD_LIKE_FILM_QUERY, filmId);
        }
        return getLikesForFilm(filmId);
    }

    @Override
    public Set<Long> deleteLike(Long userId, Long filmId) {
        if (isLikeExists(userId, filmId)) {
            update(DELETE_LIKE_FILM, userId, filmId);
            update(UPDATE_REMOVE_LIKE_QUERY, filmId);
        }
        return getLikesForFilm(filmId);
    }

    @Override
    public List<Film> top10PopularMovies(Integer count) {
        if (count == null || count <= 0) {
            count = 10;
        }
        List<Film> films = findMany(FIND_TOP_10_POPULAR_MOVIES, count);
        return films;
    }

    private Set<Long> getLikesForFilm(Long filmId) {
        return new HashSet<>(jdbc.queryForList(
                "SELECT user_id FROM like_user WHERE film_id = ?",
                Long.class,
                filmId
        ));
    }

    private boolean isLikeExists(Long userId, Long filmId) {
        return count(FIND_COUNT_LIKE_ON_FILM, userId, filmId) > 0;
    }

    private Long addFilm(Film film) {
        logger.info("Вставляем данные: name={}, description={}, releaseDate={}, duration={}, rating={}",
                film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa());
        return insert(
                INSERT_FILM,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getLikes()
        );
    }

    private void updateFilm(Film newFilm) {
        update(
                UPDATE_FILM,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                newFilm.getMpa().getId(),
                newFilm.getLikes(),
                newFilm.getId()
        );
    }

    private void addFilmGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        for (Genre genre : film.getGenres()) {
            if (!genreDbStorage.getGenreById(genre.getId()).isPresent()) {
                throw new ValidationException("Некорректный ID жанра: " + genre.getId());
            }
            update(INSERT_GENRE, film.getId(), genre.getId());
            logger.info("Добалвяем жанр в таблицу film_genre, film_id {}, genre_id {}", film.getId(), genre.getId());
        }
    }

    private void removeGenre(Long filmId) {
        update(DELETE_GENRE_QUERY, filmId);
    }
}

package ru.yandex.practicum.filmorate.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.constants.Genres;
import ru.yandex.practicum.filmorate.constants.MovieRating;
import ru.yandex.practicum.filmorate.dao.mapping.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.Like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository("filmDbStorage")
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage, LikeStorage {
    private static final String INSERT_FILM = "INSERT INTO film (name, description, release_date, duration, like_film," +
            " rating_id) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM = "UPDATE film SET name = ?, description = ?, release_date = ?, duration = ?" +
            ", like_film = ?, rating_id = ? WHERE film_id = ?";
    private static final String FIND_ALL_FILM_QUERY = "SELECT * FROM film";
    private static final String FIND_FILM_BY_ID_QUERY = "SELECT * FROM film WHERE film_id = ?";
    private static final String INSERT_GENRE_QUERY = "INSERT INTO genre (name) VALUES (?)";
    private static final String DELETE_FILM_BY_ID_QUERY = "DELETE FROM film WHERE film_id = ?";
    private static final String DELETE_FILMS_QUERY = "DELETE FROM film";
    private static final String DELETE_GENRE_QUERY = "DELETE FROM film_genre";
    private static final String FIND_COUNT_LIKE_ON_FILM = "SELECT COUNT(*) FROM like_user" +
            " WHERE user_id = ? AND film_id = ?";
    private static final String INSERT_LIKE_FILM = "INSERT INTO like_user (user_id, film_id) VALUES (?, ?)";
    private static final String UPDATE_ADD_LIKE_FILM_QUERY = "UPDATE film SET like_film = like_film + 1 WHERE film_id = ?";
    private static final String DELETE_LIKE_FILM_QUERY = "DELETE FROM like_user WHERE user_id = ? AND film_id = ?";
    private static final String UPDATE_REMOVE_LIKE_QUERY = "UPDATE film SET like_film = like_film - 1 WHERE film_id = ?";
    private static final String FIND_TOP_10_POPULAR_MOVIES = "SELECT * FROM film ORDER BY like_film DESC LIMIT 10";
    private static final String INSERT_GENRE = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    public static final String FIND_RATING_QUERY = "SELECT * FROM rating WHERE rating_id = ?";
    private static final String INSERT_RATING_QUERY = "INSERT INTO rating (rating_id, name) VALUES (?, ?)";
    private static final String CHECK_RATING_EXISTS = "SELECT COUNT(*) FROM rating WHERE rating_id = ?";
    private static final String FIND_ALL_RATING_QUERY = "SELECT * FROM rating";

    private static final Logger logger = LoggerFactory.getLogger(FilmDbStorage.class);

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc, mapper);
        initializeRatings();
        initializeGenres();
    }

    @Override
    public Film create(Film film) {
        try {
            logger.info("Переданные данные для создания фильма: rating={}, genres={}", film.getMpa(), film.getGenres());
            logger.info("Создание фильма: {}", film);

            Long id = addFilm(film);
            film.setId(id);
            logger.info("Создание фильма с ID: {}", id);
            addGenre(film);
            logger.info("Текущие рейтинги в базе: {}", jdbc.queryForList("SELECT * FROM rating"));
            return film;
        } catch (Exception e) {
            logger.error("Ошибка при добавлении фильма: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось добавить фильм");
        }
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
        update(DELETE_GENRE_QUERY);
        return delete(DELETE_FILM_BY_ID_QUERY, filmId);
    }

    @Override
    public void clearFilm() {
        update(DELETE_GENRE_QUERY);
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

    @Override
    public MovieRating getRatingNameById(int mpaId) {
        logger.info("Запрос рейтинга с ID: {}", mpaId);

        Mpa rating = jdbc.queryForObject(FIND_RATING_QUERY, new Object[]{mpaId}, (rs, rowNum) -> {
            int id = rs.getInt("rating_id");
            return new Mpa(id);
        });
        return MovieRating.fromId(rating.getId());
    }

    @Override
    public List<MovieRating> getAllRating() {
        return jdbc.query(FIND_ALL_RATING_QUERY, (rs, rowNum) -> {
            int ratingId = rs.getInt("rating_id");
            return MovieRating.fromId(ratingId);
        });
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

    private Long addFilm(Film film) {
        logger.info("Вставляем данные: name={}, description={}, releaseDate={}, duration={}, likeFilm={}, rating={}",
                film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getLikes(), film.getMpa());
        return insert(
                INSERT_FILM,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getLikes(),
                film.getMpa().getId()
        );
    }

    private void updateFilm(Film newFilm) {
        update(
                UPDATE_FILM,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                newFilm.getLikes(),
                newFilm.getMpa().getId(),
                newFilm.getId()
        );
    }

    private void initializeRatings() {
        for (MovieRating rating : MovieRating.values()) {
            try {
                logger.info("Рейтинг {} с ID {} добавлен.", rating.name(), rating.getId());
                addRating(rating);
            } catch (ValidationException e) {
                logger.info("Рейтинг {} с ID {} уже существует.", rating.name(), rating.getId());
                throw new ValidationException("Рейтинг уже существует: " + rating.getId());
            }
        }
    }

    private void addRating(MovieRating rating) {
        try {
            int count = jdbc.queryForObject(CHECK_RATING_EXISTS, new Object[]{rating.getId()}, Integer.class);
            if (count == 0) {
                jdbc.update(INSERT_RATING_QUERY, rating.getId(), rating.name());
                logger.info("Рейтинг {} с ID {} добавлен.", rating.name(), rating.getId());
            } else {
                logger.info("Рейтинг {} с ID {} уже существует.", rating.name(), rating.getId());
            }
        } catch (Exception e) {
            logger.error("Ошибка при добавлении рейтинга: {}", e.getMessage());
            throw new ValidationException("Неккоректынй идентификатор рейтинга: " + rating.getId());
        }
    }

    private void addGenre(Film film) {
        List<Genre> genres = film.getGenres();
        genres.forEach(genre -> addGenreInFilmGenreDB(film.getId(), genre.getId()));
    }

    private void initializeGenres() {
        for (Genres genres : Genres.values()) {
            for (Film film : getAllFilms()) {
                if (!genreExists(film.getId(), genres.getId())) {
                    try {
                        addGenreInFilmGenreDB(film.getId(), genres.getId());
                    } catch (ValidationException e) {
                        logger.error("Ошибка при добавлении жанра: {}", e.getMessage());
                        throw new ValidationException("Ошибка при добавлении жанра.");
                    }
                }
            }
        }
    }

    private boolean genreExists(Long filmId, Long genreId) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM genre WHERE genre_id = ?", Long.class, genreId);
        return count != null && count > 0;
    }

    private void addGenreInFilmGenreDB(Long filmId, Long genreId) {
        logger.debug("Вставка данных film_genre: film_id={}, genre_id={}", filmId, genreId);
        jdbc.update(INSERT_GENRE, filmId, genreId);
    }

    private void addGenreToDatabase(Genres genre) {
        logger.debug("Вставка жанра: {}", genre.getName());
        jdbc.update(INSERT_GENRE_QUERY, genre.getName());
    }

    private Long getOrAddGenre(Long filmId, Genres genre) {
        if (genreExists(filmId, genre.getId())) {
            return genre.getId();
        } else {
            Long newGenreId = insert(INSERT_GENRE_QUERY, genre.getName());
            return newGenreId;
        }
    }
}

package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.mapping.FilmRowMapper;
import ru.yandex.practicum.filmorate.dao.mapping.GenreRowMapper;
import ru.yandex.practicum.filmorate.dao.mapping.MpaRowMapper;
import ru.yandex.practicum.filmorate.dao.mapping.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.RatingDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, UserDbStorage.class, UserRowMapper.class, FilmRowMapper.class,
        RatingDbStorage.class, MpaRowMapper.class, GenreDbStorage.class, GenreRowMapper.class})
public class FilmDbTest {

    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbcTemplate;
    private final RatingDbStorage ratingDbStorage;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.execute("DELETE FROM film_genre");
        jdbcTemplate.execute("DELETE FROM film");
        jdbcTemplate.execute("DELETE FROM user_app");
        jdbcTemplate.execute("DELETE FROM rating");
        jdbcTemplate.execute("DELETE FROM genre");
        jdbcTemplate.execute("DELETE FROM like_user");
        jdbcTemplate.execute("DELETE FROM friend_user");
        jdbcTemplate.execute("INSERT INTO genre(genre_id, name) VALUES (1, 'Comedy')");
        jdbcTemplate.update("INSERT INTO genre(genre_id, name) VALUES (2, 'Drama')");
        jdbcTemplate.update("INSERT INTO genre(genre_id, name) VALUES (3, 'Horror')");
        jdbcTemplate.execute("INSERT INTO rating (rating_id, name) VALUES (1, 'G')");
    }

    @Test
    public void shouldSaveFilmInDb() {
        Genre comedy = new Genre(1L, "Comedy");
        Set<Genre> genreList = Set.of(comedy);
        Optional<Mpa> getRating = ratingDbStorage.getRatingById(1);
        Mpa getMpa = getRating.orElseThrow(() -> new NotFoundException("Рейтинг не найден"));
        Film film = Film.builder()
                .name("Titanic")
                .description("Description Long")
                .releaseDate(LocalDate.of(1990, 1, 10))
                .duration(180L)
                .likes(20)
                .genres(genreList)
                .mpa(getMpa)
                .build();

        Film createdFilm = filmDbStorage.create(film);

        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo("Titanic");
        assertThat(createdFilm.getDescription()).isEqualTo("Description Long");
        assertThat(createdFilm.getReleaseDate()).isEqualTo(LocalDate.of(1990, 1, 10));
        assertThat(createdFilm.getDuration()).isEqualTo(180L);
        assertThat(createdFilm.getLikes()).isEqualTo(20);
        assertThat(createdFilm.getMpa().getName()).isEqualTo(createdFilm.getMpa().getName());
        assertThat(createdFilm.getId()).isNotNull();

    }

    @Test
    public void shouldUpdateFilmDb() {
        Genre comedy = new Genre(1L, "Comedy");
        Set<Genre> genreList = Set.of(comedy);
        Optional<Mpa> getRating = ratingDbStorage.getRatingById(1);
        Mpa getMpa = getRating.orElseThrow(() -> new NotFoundException("Рейтинг не найден"));

        Film film = filmDbStorage.create(Film.builder()
                .name("Titanic")
                .description("Description Long")
                .releaseDate(LocalDate.of(1990, 1, 10))
                .duration(180L)
                .likes(20)
                .genres(genreList)
                .mpa(getMpa)
                .build());
        film.setDescription("Leo Di");
        Film filmUpdate = filmDbStorage.update(film);

        assertThat(filmUpdate).isNotNull();
        assertThat(filmUpdate.getName()).isEqualTo("Titanic");
        assertThat(filmUpdate.getDescription()).isEqualTo("Leo Di");
        assertThat(filmUpdate.getReleaseDate()).isEqualTo(LocalDate.of(1990, 1, 10));
        assertThat(filmUpdate.getDuration()).isEqualTo(180L);
        assertThat(film.getLikes()).isEqualTo(20);
        assertThat(film.getGenres()).isEqualTo(genreList);
        assertThat(film.getMpa().getName()).isEqualTo(filmUpdate.getMpa().getName());
        assertThat(filmUpdate.getId()).isNotNull();
    }

    @Test
    public void shouldGetAllFilmsDb() {
        Genre comedy = new Genre(1L, "Comedy");
        Set<Genre> genreList = Set.of(comedy);
        Optional<Mpa> getRating = ratingDbStorage.getRatingById(1);
        Mpa getMpa = getRating.orElseThrow(() -> new NotFoundException("Рейтинг не найден"));
        Film film = filmDbStorage.create(Film.builder()
                .name("Titanic")
                .description("Description Long")
                .releaseDate(LocalDate.of(1990, 1, 10))
                .duration(180L)
                .likes(20)
                .genres(genreList)
                .mpa(getMpa)
                .build());

        List<Film> films = filmDbStorage.getAllFilms();
        assertThat(films).isNotNull();
    }

    @Test
    public void shouldFindByIdFilmDb() {
        Genre comedy = new Genre(1L, "Comedy");
        Set<Genre> genreList = Set.of(comedy);
        Optional<Mpa> getRating = ratingDbStorage.getRatingById(1);
        Mpa getMpa = getRating.orElseThrow(() -> new NotFoundException("Рейтинг не найден"));
        Film film = filmDbStorage.create(Film.builder()
                .name("Titanic")
                .description("Description Long")
                .releaseDate(LocalDate.of(1990, 1, 10))
                .duration(180L)
                .likes(20)
                .genres(genreList)
                .mpa(getMpa)
                .build());

        Optional<Film> findFilmById = filmDbStorage.findById(film.getId());
        assertThat(findFilmById).isNotNull();
        assertThat(findFilmById.get().getName()).isEqualTo("Titanic");
    }

    @Test
    public void shouldDeleteFilmById() {
        Genre comedy = new Genre(1L, "Comedy");
        Set<Genre> genreList = Set.of(comedy);
        Optional<Mpa> getRating = ratingDbStorage.getRatingById(1);
        Mpa getMpa = getRating.orElseThrow(() -> new NotFoundException("Рейтинг не найден"));
        Film film = filmDbStorage.create(Film.builder()
                .name("Titanic")
                .description("Description Long")
                .releaseDate(LocalDate.of(1990, 1, 10))
                .duration(180L)
                .likes(20)
                .genres(genreList)
                .mpa(getMpa)
                .build());

        assertThat(filmDbStorage.findById(film.getId())).isPresent();

        boolean deleteFilm = filmDbStorage.deleteFilmById(film.getId());

        assertThat(deleteFilm).isTrue();

        assertThat(filmDbStorage.findById(film.getId())).isNotPresent();
    }

    @Test
    public void shouldClearFilmsDb() {
        Genre comedy = new Genre(1L, "Comedy");
        Set<Genre> genreList = Set.of(comedy);
        Optional<Mpa> getRating = ratingDbStorage.getRatingById(1);
        Mpa getMpa = getRating.orElseThrow(() -> new NotFoundException("Рейтинг не найден"));
        Film film = filmDbStorage.create(Film.builder()
                .name("Titanic")
                .description("Description Long")
                .releaseDate(LocalDate.of(1990, 1, 10))
                .duration(180L)
                .likes(20)
                .genres(genreList)
                .mpa(getMpa)
                .build());
        filmDbStorage.clearFilm();
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM film", Integer.class);
        assertThat(count).isEqualTo(0);
    }

//    @Test
//    public void shouldAddLikeFilmDb() {
//        Genre comedy = new Genre(1L, "Comedy");
//        Set<Genre> genreList = Set.of(comedy);
//        Optional<Mpa> getRating = ratingDbStorage.getRatingById(1);
//        Mpa getMpa = getRating.orElseThrow(() -> new NotFoundException("Рейтинг не найден"));
//        User user = userDbStorage.create(User.builder()
//                .name("User1")
//                .email("user1@example.com")
//                .login("testlogin1")
//                .birthday(LocalDate.of(1990, 1, 1))
//                .build());
//
//        Film film = filmDbStorage.create(Film.builder()
//                .name("Titanic")
//                .description("Description Long")
//                .releaseDate(LocalDate.of(1990, 1, 10))
//                .duration(180L)
//                .genres(genreList)
//                .likes(0)
//                .mpa(getMpa)
//                .build());
//
//        filmDbStorage.addLike(user.getId(), film.getId());
//
//        Optional<Film> updatedFilm = filmDbStorage.findById(film.getId());
//        System.out.println("Обновленные лайки: " + updatedFilm.get().getLikes());
//
//        assertThat(updatedFilm.get().getLikes()).isEqualTo(1);
//    }
//
//    @Test
//    public void shouldDeleteLikeFilmDb() {
//        Genre comedy = new Genre(1L, "Comedy");
//        List<Genre> genreList = List.of(comedy);
//        User user = userDbStorage.create(User.builder()
//                .name("User1")
//                .email("user1@example.com")
//                .login("testlogin1")
//                .birthday(LocalDate.of(1990, 1, 1))
//                .build());
//
//        Film film = filmDbStorage.create(Film.builder()
//                .name("Titanic")
//                .description("Description Long")
//                .releaseDate(LocalDate.of(1990, 1, 10))
//                .duration(180L)
//                .likes(20)
//                .genres(genreList)
//                .mpa(MovieRating.G)
//                .build());
//
//        filmDbStorage.addLike(film.getId(), user.getId());
//        filmDbStorage.deleteLike(film.getId(), user.getId());
//
//        Optional<Film> updatedFilm = filmDbStorage.findById(film.getId());
//        assertThat(updatedFilm.get().getLikes()).isEqualTo(20);
//    }
//
//
//    @Test
//    public void shouldGetRating() {
//        MovieRating movieRating = MovieRating.fromId(1);
//        assertThat(MovieRating.G).isEqualTo(movieRating);
//    }
//
//    @Test
//    public void shouldGetAllRating() {
//        List<MovieRating> ratings = filmDbStorage.getAllRating();
//        assertThat(ratings).hasSize(1);
//    }
}

package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.constants.MovieRating;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.dao.mapping.FilmRowMapper;
import ru.yandex.practicum.filmorate.dao.mapping.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, UserDbStorage.class, UserRowMapper.class, FilmRowMapper.class})
public class FilmDbTest {

    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.execute("DELETE FROM film");
        jdbcTemplate.execute("DELETE FROM user_app");
        jdbcTemplate.execute("INSERT INTO genre (genre_id, name) VALUES (1, 'Drama')");
        jdbcTemplate.execute("INSERT INTO genre (genre_id, name) VALUES (2, 'Comedy')");
        jdbcTemplate.execute("INSERT INTO genre (genre_id, name) VALUES (3, 'Horror')");
    }

    @Test
    public void shouldSaveFilmInDb() {
        Genre comedy = new Genre(2L, "Comedy");
        Film film = filmDbStorage.create(Film.builder()
                .name("Titanic")
                .description("Description Long")
                .releaseDate(LocalDate.of(1990, 1, 10))
                .duration(180L)
                .likes(20)
                .genre(comedy)
                .rating(MovieRating.PG)
                .build());

        assertThat(film).isNotNull();

        assertThat(film.getName()).isEqualTo("Titanic");
        assertThat(film.getDescription()).isEqualTo("Description Long");
        assertThat(film.getReleaseDate()).isEqualTo(LocalDate.of(1990, 1, 10));
        assertThat(film.getDuration()).isEqualTo(180L);
        assertThat(film.getLikes()).isEqualTo(20);
        assertThat(film.getGenre()).isEqualTo(comedy);
        assertThat(film.getRating()).isEqualTo(MovieRating.PG);

        assertThat(film.getId()).isNotNull();

    }

    @Test
    public void shouldUpdateFilmDb() {
        Genre comedy = new Genre(2L, "Comedy");
        Film film = filmDbStorage.create(Film.builder()
                .name("Titanic")
                .description("Description Long")
                .releaseDate(LocalDate.of(1990, 1, 10))
                .duration(180L)
                .likes(20)
                .genre(comedy)
                .rating(MovieRating.PG)
                .build());
        film.setDescription("Leo Di");
        Film filmUpdate = filmDbStorage.update(film);

        assertThat(filmUpdate).isNotNull();
        assertThat(filmUpdate.getName()).isEqualTo("Titanic");
        assertThat(filmUpdate.getDescription()).isEqualTo("Leo Di");
        assertThat(filmUpdate.getReleaseDate()).isEqualTo(LocalDate.of(1990, 1, 10));
        assertThat(filmUpdate.getDuration()).isEqualTo(180L);
        assertThat(film.getLikes()).isEqualTo(20);
        assertThat(film.getGenre()).isEqualTo(comedy);
        assertThat(film.getRating()).isEqualTo(MovieRating.PG);
        assertThat(filmUpdate.getId()).isNotNull();
    }

    @Test
    public void shouldGetAllFilmsDb() {
        Genre comedy = new Genre(2L, "Comedy");
        Film film = filmDbStorage.create(Film.builder()
                .name("Titanic")
                .description("Description Long")
                .releaseDate(LocalDate.of(1990, 1, 10))
                .duration(180L)
                .likes(20)
                .genre(comedy)
                .rating(MovieRating.PG)
                .build());

        List<Film> films = filmDbStorage.getAllFilms();
        assertThat(films).hasSize(1);
        assertThat(films).extracting("name")
                .containsExactlyInAnyOrder("Titanic");
    }

    @Test
    public void shouldFindByIdFilmDb() {
        Genre comedy = new Genre(2L, "Comedy");
        Film film = filmDbStorage.create(Film.builder()
                .name("Titanic")
                .description("Description Long")
                .releaseDate(LocalDate.of(1990, 1, 10))
                .duration(180L)
                .likes(20)
                .genre(comedy)
                .rating(MovieRating.PG)
                .build());

        Optional<Film> findFilmById = filmDbStorage.findById(film.getId());
        assertThat(findFilmById).isNotNull();
        assertThat(findFilmById.get().getName()).isEqualTo("Titanic");
    }

    @Test
    public void shouldDeleteFilmById() {
        Genre comedy = new Genre(2L, "Comedy");
        Film film = filmDbStorage.create(Film.builder()
                .name("Titanic")
                .description("Description Long")
                .releaseDate(LocalDate.of(1990, 1, 10))
                .duration(180L)
                .likes(20)
                .genre(comedy)
                .rating(MovieRating.PG)
                .build());
        assertThat(filmDbStorage.findById(film.getId()).isPresent());
        boolean deleteFilm = filmDbStorage.deleteFilmById(film.getId());
        assertThat(deleteFilm).isTrue();
    }

    @Test
    public void shouldClearFilmsDb() {
        Genre comedy = new Genre(2L, "Comedy");
        Film film = filmDbStorage.create(Film.builder()
                .name("Titanic")
                .description("Description Long")
                .releaseDate(LocalDate.of(1990, 1, 10))
                .duration(180L)
                .likes(20)
                .genre(comedy)
                .rating(MovieRating.PG)
                .build());
        filmDbStorage.clearFilm();
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM film", Integer.class);
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void shouldAddLikeFilmDb() {
        Genre comedy = new Genre(2L, "Comedy");
        User user = userDbStorage.create(User.builder()
                .name("User1")
                .email("user1@example.com")
                .login("testlogin1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        Film film = filmDbStorage.create(Film.builder()
                .name("Titanic")
                .description("Description Long")
                .releaseDate(LocalDate.of(1990, 1, 10))
                .duration(180L)
                .likes(20)
                .genre(comedy)
                .rating(MovieRating.PG)
                .build());

        filmDbStorage.addLike(film.getId(), user.getId());

        Optional<Film> updatedFilm = filmDbStorage.findById(film.getId());
        assertThat(updatedFilm.get().getLikes()).isEqualTo(21);
    }

    @Test
    public void shouldDeleteLikeFilmDb() {
        Genre comedy = new Genre(2L, "Comedy");
        User user = userDbStorage.create(User.builder()
                .name("User1")
                .email("user1@example.com")
                .login("testlogin1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        Film film = filmDbStorage.create(Film.builder()
                .name("Titanic")
                .description("Description Long")
                .releaseDate(LocalDate.of(1990, 1, 10))
                .duration(180L)
                .likes(20)
                .genre(comedy)
                .rating(MovieRating.PG)
                .build());

        filmDbStorage.addLike(film.getId(), user.getId());
        filmDbStorage.deleteLike(film.getId(), user.getId());

        Optional<Film> updatedFilm = filmDbStorage.findById(film.getId());
        assertThat(updatedFilm.get().getLikes()).isEqualTo(20);
    }

    @Test
    public void shouldGetTopFilmsInLikes() {
        Genre drama = new Genre(1L, "Drama");
        Genre comedy = new Genre(2L, "Comedy");
        Genre horror = new Genre(3L, "Horror");
        Film film1 = filmDbStorage.create(Film.builder()
                .name("Titanic")
                .description("Description Long")
                .releaseDate(LocalDate.of(1990, 1, 10))
                .duration(180L)
                .likes(20)
                .genre(drama)
                .rating(MovieRating.PG)
                .build());

        Film film2 = filmDbStorage.create(Film.builder()
                .name("Robokop")
                .description("Description Long")
                .releaseDate(LocalDate.of(1990, 1, 10))
                .duration(180L)
                .likes(30)
                .genre(comedy)
                .rating(MovieRating.PG)
                .build());

        Film film3 = filmDbStorage.create(Film.builder()
                .name("House")
                .description("Description Long")
                .releaseDate(LocalDate.of(1990, 1, 10))
                .duration(180L)
                .likes(29)
                .genre(horror)
                .rating(MovieRating.PG)
                .build());

        List<Film> topPopularFilm = filmDbStorage.top10PopularMovies(3);


        assertThat(topPopularFilm).hasSize(3);
        assertThat(topPopularFilm).containsExactlyInAnyOrder(film2, film3, film1);
        assertThat(topPopularFilm).containsExactly(film2, film3, film1);
    }
}

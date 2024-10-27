package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.FriendshipService;
import ru.yandex.practicum.filmorate.service.LikeService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@SpringBootTest
class FilmorateApplicationTests {
    private static final LocalDate birthday = LocalDate.of(2001, 10, 9);


    private FilmController filmController;
    @Autowired
    private FilmService filmService;
    @Autowired
    private LikeService likeService;

    private UserController userController;
    @Autowired
    private FriendshipService friendshipService;
    @Autowired
    private UserService userService;

    @BeforeEach
    public void setUp() {
        this.userController = new UserController(userService, friendshipService);
        this.filmController = new FilmController(filmService, likeService);
        filmService.clearFilm();
        userService.clearUser();
    }

    @Test
    public void shouldCreateUser() {
        User user = User.builder()
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        User userControllerCreate = userController.create(user);

        Assertions.assertNotNull(userControllerCreate.getId(), "ID должен быть установлен");
        Assertions.assertNotNull(user.getId(), "ID должен быть установлен");
        Assertions.assertEquals(user, userControllerCreate, "Пользователи должны быть одинаковы");
    }

    @Test
    public void shouldUpdateUser() {
        User user = User.builder()
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smool@yandex.ru")
                .birthday(birthday)
                .build();

        userController.create(user);
        user.setName("kroki");

        User userControllerUpdate = userController.update(user);

        Assertions.assertEquals(user, userControllerUpdate, "Пользователи должны быть одинаковы");
    }

    @Test
    public void shouldGetAllUsers() {
        User user = User.builder()
                .id(1L)
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        userController.create(user);

        User user2 = User.builder()
                .id(2L)
                .name("AntonCa")
                .login("Anton")
                .email("anon.anton@yandex.ru")
                .birthday(birthday)
                .build();

        userController.create(user2);

        List<User> usersList = userController.allListUsers();

        Assertions.assertEquals(2, usersList.size(), "Должно быть два пользователя");
    }

    @Test
    public void shouldntBirthdayToBeInAfter() {
        LocalDate localDate = LocalDate.of(2025, 11, 12);
        User user = User.builder()
                .id(1L)
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(localDate)
                .build();

        Assertions.assertThrows(ValidationException.class, () -> {
            userController.create(user);
        });
    }

    @Test
    public void shouldntEmailToBeEmptyAndNoHaveChar() {
        User user = User.builder()
                .id(1L)
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smolyandex.ru")
                .birthday(birthday)
                .build();

        Assertions.assertThrows(ValidationException.class, () -> {
            userController.create(user);
        });
    }

    @Test
    public void shouldntLoginToBeEmptyAndHaveSpaces() {
        User user = User.builder()
                .id(1L)
                .name("Smolcap")
                .login("")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        Assertions.assertThrows(ValidationException.class, () -> {
            userController.create(user);
        });
    }

    @Test
    public void shouldNameToBeEmptyButUseLogin() {
        User user = User.builder()
                .id(1L)
                .name(" ")
                .login("daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        User create = userController.create(user);
        Assertions.assertEquals("daniel", create.getName(), "Если имя пустое в таком случае будет " +
                "использован логин");
    }

    @Test
    public void shouldAddFriend() {
        User user = User.builder()
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        User userControllerCreate = userController.create(user);
        Long getIdUsr1 = userControllerCreate.getId();

        User user2 = User.builder()
                .name("StepaCap")
                .login("Stemap")
                .email("dany.smo@yandex.ru")
                .birthday(birthday)
                .build();

        User userControllerCreate2 = userController.create(user2);
        Long getIdUser2 = userControllerCreate2.getId();

        Set<Long> setController = userController.addFriend(getIdUsr1, getIdUser2);

        Assertions.assertTrue(setController.contains(getIdUser2), "Список друзей должен содержать" +
                " идентификатор нового друга");
        Assertions.assertTrue(userController.findById(getIdUsr1).getFriends().contains(getIdUser2), "Пользов" +
                "атель с ID 2 должен находиться в друзьях у пользователя с ID 1");
        Assertions.assertTrue(userController.findById(getIdUser2).getFriends().contains(getIdUsr1), "Пользов" +
                "атель с ID 1 должен находиться в друзьях у пользователя с ID 2");
    }

    @Test
    public void shouldAddFriendAlreadyFriends() {
        User user = User.builder()
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        User userControllerCreate = userController.create(user);
        Long getIdUsr1 = userControllerCreate.getId();

        User user2 = User.builder()
                .name("StepaCap")
                .login("Stemap")
                .email("dany.smo@yandex.ru")
                .birthday(birthday)
                .build();

        User userControllerCreate2 = userController.create(user2);
        Long getIdUser2 = userControllerCreate2.getId();

        userController.addFriend(getIdUsr1, getIdUser2);
        Exception exception = Assertions.assertThrows(ValidationException.class, () -> {
            userController.addFriend(1L, 2L);
        });
        Assertions.assertEquals("Пользователь уже является другом", exception.getMessage());
    }

    @Test
    public void notShouldAddFriendWhenUserNotFound() {
        Exception exception = Assertions.assertThrows(NotFoundException.class, () -> {
            userController.addFriend(1L, 2L);
        });
        Assertions.assertEquals("Пользователь с ID " + 1L + " не найден", exception.getMessage());
    }

    @Test
    public void notShouldRemoveFriendNotFound() {
        Exception exception = Assertions.assertThrows(NotFoundException.class, () -> {
            userController.deleteFriend(1L, 2L);
        });
        Assertions.assertEquals("Пользователь с ID " + 1L + " не найден", exception.getMessage());
    }

    @Test
    public void shouldGetMutualFriends() {
        User user = User.builder()
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        User userControllerCreate = userController.create(user);
        Long getIdUsr1 = userControllerCreate.getId();

        User user2 = User.builder()
                .name("StepaCap")
                .login("Stemap")
                .email("dany.smo@yandex.ru")
                .birthday(birthday)
                .build();

        User userControllerCreate2 = userController.create(user2);
        Long getIdUser2 = userControllerCreate2.getId();

        User user3 = User.builder()
                .name("Genadii")
                .login("Stemap")
                .email("genka.smo@yandex.ru")
                .birthday(birthday)
                .build();

        User userControllerCreate3 = userController.create(user3);
        Long getIdUser3 = userControllerCreate3.getId();
        userController.addFriend(getIdUsr1, getIdUser3);
        userController.addFriend(getIdUser2, getIdUser3);

        List<User> mutualFriend = userController.getListMutualFriend(getIdUsr1, getIdUser2);
        Assertions.assertEquals(1, mutualFriend.size(), "Пользователь с ID " + getIdUsr1 +
                " и ID " + getIdUser2 + " имеют одного общего друга");
    }

    @Test
    public void notShouldGetMutualFriendWhenUserNotFound() {
        Exception exception = Assertions.assertThrows(NotFoundException.class, () -> {
            List<User> mutualFriend = userController.getListMutualFriend(1L, 2L);
            Assertions.assertTrue(mutualFriend.isEmpty(), "Список пользователь должен быть пуст");
        });
        Assertions.assertEquals("Пользователь с ID " + 1L + " не найден", exception.getMessage());
    }

    @Test
    public void shouldDeleteFriend() {
        User user = User.builder()
                .id(1L)
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        User userControllerCreate = userController.create(user);
        Long getIdUsr1 = userControllerCreate.getId();

        User user2 = User.builder()
                .id(2L)
                .name("StepaCap")
                .login("Stemap")
                .email("dany.smo@yandex.ru")
                .birthday(birthday)
                .build();

        User userControllerCreate2 = userController.create(user2);
        Long getIdUser2 = userControllerCreate2.getId();

        userController.addFriend(getIdUsr1, getIdUser2);

        Set<Long> setController = userController.deleteFriend(getIdUsr1, getIdUser2);

        Assertions.assertFalse(setController.contains(getIdUser2), "Список друзей должен быть пуст");
    }

    @Test
    public void shouldGetListFriendsById() {
        User user = User.builder()
                .id(1L)
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        User userControllerCreate = userController.create(user);
        Long getIdUsr1 = userControllerCreate.getId();

        User user2 = User.builder()
                .id(2L)
                .name("StepaCap")
                .login("Stemap")
                .email("dany.smo@yandex.ru")
                .birthday(birthday)
                .build();

        User userControllerCreate2 = userController.create(user2);
        Long getIdUser2 = userControllerCreate2.getId();

        userController.addFriend(getIdUsr1, getIdUser2);
        List<User> listFriends = userController.getFriends(getIdUsr1);
        List<User> listFriendsTwo = userController.getFriends(getIdUser2);

        Assertions.assertEquals(1, listFriends.size(), "Список должен содержать одного друга");
        Assertions.assertTrue(listFriends.contains(userControllerCreate2), "Должен находиться пользователь " +
                "с ID " + getIdUser2);
        Assertions.assertTrue(listFriendsTwo.contains(userControllerCreate), "Должен находиться пользователь " +
                "с ID " + getIdUsr1);
    }

    @Test
    public void shouldMutualListFriends() {
        User user1 = User.builder()
                .id(1L)
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        userController.create(user1);

        User user2 = User.builder()
                .id(2L)
                .name("StepaCap")
                .login("Stemap")
                .email("dany.smo@yandex.ru")
                .birthday(birthday)
                .build();
        userController.create(user2);

        User user3 = User.builder()
                .id(3L)
                .name("StepaCap")
                .login("Stemap")
                .email("dany.smo@yandex.ru")
                .birthday(birthday)
                .build();

        userController.create(user3);

        userController.addFriend(user1.getId(), user2.getId());
        userController.addFriend(user1.getId(), user3.getId());
        userController.addFriend(user2.getId(), user3.getId());

        List<User> mutualFriends = userController.getListMutualFriend(user1.getId(), user2.getId());
        Assertions.assertTrue(mutualFriends.contains(user3), "Общим другом должен быть User3");
    }

    @Test
    public void shouldCreateFilm() {
        LocalDate localDate = LocalDate.of(1997, 12, 16);
        Film film = Film.builder()
                .id(1L)
                .name("Titanic")
                .description("Description Long")
                .releaseDate(localDate)
                .duration(190L)
                .build();

        Film filmControllerCreate = filmController.create(film);

        Assertions.assertEquals(film, filmControllerCreate, "Фильмы должны быть одинаковы");
    }

    @Test
    public void shouldUpdateFilm() {
        LocalDate localDate = LocalDate.of(1997, 12, 16);
        Film film = Film.builder()
                .id(1L)
                .name("Titanic")
                .description("Description Long")
                .releaseDate(localDate)
                .duration(190L)
                .build();
        filmController.create(film);

        film = Film.builder()
                .id(1L)
                .name("Titanic and Lodka")
                .description("Description Long")
                .releaseDate(localDate)
                .duration(190L)
                .build();

        Film filmControllerUpdate = filmController.update(film);
        Assertions.assertEquals(film, filmControllerUpdate, "Фильм должен соответствовать обновлённому");
    }

    @Test
    public void shouldGetAllFilms() {
        LocalDate localDate = LocalDate.of(1997, 12, 16);
        Film film = Film.builder()
                .id(1L)
                .name("Titanic")
                .description("Description Long")
                .releaseDate(localDate)
                .duration(190L)
                .build();
        filmController.create(film);

        List<Film> listFilms = filmController.allListFilm();

        Assertions.assertEquals(1, listFilms.size(), "Список должен содержать 1 фильм");
    }

    @Test
    public void shouldntNameFilmToBeEmpty() {
        LocalDate localDate = LocalDate.of(1997, 12, 16);
        Film film = Film.builder()
                .name("")
                .description("Description Long")
                .releaseDate(localDate)
                .duration(190L)
                .build();

        Assertions.assertThrows(ValidationException.class, () -> {
            filmController.create(film);
        });
    }

    @Test
    public void shouldMaxLongDescription200Char() {
        LocalDate localDate = LocalDate.of(1997, 12, 16);
        Duration duration = Duration.ofMinutes(194);
        Film film = Film.builder()
                .name("Titanic")
                .description("Слишком длинное описание, которое превышает допустимую длину ........................." +
                        "...................." + ".........................................." +
                        ".....................................................")
                .releaseDate(localDate)
                .duration(190L)
                .build();

        Assertions.assertThrows(ValidationException.class, () -> {
            filmController.create(film);
        });
    }

    @Test
    public void shouldReleaseDateNotBefore28December1985Year() {
        LocalDate localDate = LocalDate.of(1885, 12, 16);
        Duration duration = Duration.ofMinutes(194);
        Film film = Film.builder()
                .name("Titanic")
                .description("Description Long")
                .releaseDate(localDate)
                .duration(190L)
                .build();

        Assertions.assertThrows(ValidationException.class, () -> {
            filmController.create(film);
        });
    }

    @Test
    public void shouldDurationFilmInPositive() {
        LocalDate localDate = LocalDate.of(1997, 12, 16);
        Film film = Film.builder()
                .name("Titanic")
                .description("Description Long")
                .releaseDate(localDate)
                .duration(-178L)
                .build();

        Assertions.assertThrows(ValidationException.class, () -> {
            filmController.create(film);
        });
    }

    @Test
    public void shouldAddLike() {
        LocalDate localDate = LocalDate.of(1997, 12, 16);
        User user = User.builder()
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        User userControllerCreate = userController.create(user);

        Film film = Film.builder()
                .name("Titanic")
                .description("Description Long")
                .releaseDate(localDate)
                .duration(180L)
                .build();
        filmController.create(film);
        Set<Long> updatedLikes = filmController.addLikeFilms(film.getId(), userControllerCreate.getId());
        System.out.println("Update likes: " + updatedLikes);

        Assertions.assertTrue(updatedLikes.contains(userControllerCreate.getId()), "Лайк не был установлен для фильма.");
    }

    @Test
    public void shouldDeleteLike() {
        LocalDate localDate = LocalDate.of(1997, 12, 16);
        User user = User.builder()
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        User userControllerCreate = userController.create(user);

        Film film = Film.builder()
                .name("Titanic")
                .description("Description Long")
                .releaseDate(localDate)
                .duration(180L)
                .build();
        filmController.create(film);
        filmController.addLikeFilms(film.getId(), userControllerCreate.getId());
        Set<Long> currentLike = filmService.findById(film.getId()).getUserLikes();
        System.out.println("Current likes before deletion: " + currentLike);

        Set<Long> updatedLikes = filmController.deleteLike(film.getId(), userControllerCreate.getId());

        Assertions.assertFalse(updatedLikes.contains(userControllerCreate.getId()), "Лайк не был удален " +
                "для фильма");
    }

    @Test
    public void shouldGetTop10Films() {
        LocalDate localDate = LocalDate.of(1997, 12, 16);
        Film film1 = Film.builder()
                .name("Titanic")
                .description("Description Long")
                .releaseDate(localDate)
                .duration(180L)
                .likes(20)
                .build();
        filmController.create(film1);

        Film film2 = Film.builder()
                .name("Robokop")
                .description("Description Long")
                .releaseDate(localDate)
                .duration(180L)
                .likes(10)
                .build();
        filmController.create(film2);

        Film film3 = Film.builder()
                .name("Interstellar")
                .description("Description Long")
                .releaseDate(localDate)
                .duration(180L)
                .likes(30)
                .build();
        filmController.create(film3);
        List<Film> popularFilm = filmController.topFilms(10);

        Assertions.assertEquals(3, popularFilm.size(), "Количество фильмов должно быть 3");
        Assertions.assertEquals("Interstellar", popularFilm.get(0).getName());
        Assertions.assertEquals("Titanic", popularFilm.get(1).getName());
        Assertions.assertEquals("Robokop", popularFilm.get(2).getName());
    }
}


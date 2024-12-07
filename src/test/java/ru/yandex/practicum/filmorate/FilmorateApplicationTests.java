package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.config.TestConfig;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.FriendshipService;
import ru.yandex.practicum.filmorate.service.LikeService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@SpringBootTest(classes = {FilmorateApplicationTests.class, TestConfig.class})
@ActiveProfiles("test")
class FilmorateApplicationTests {
    private static final LocalDate birthday = LocalDate.of(2001, 10, 9);
    private final Mpa defaultMpa = new Mpa();

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
        NewUserRequest request = NewUserRequest.builder()
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        UserDto createdUserDto = userController.create(request);

        Assertions.assertNotNull(createdUserDto.getId(), "ID должен быть установлен");
        Assertions.assertEquals(request.getName(), createdUserDto.getName(), "Имя должно совпадать");
    }

    @Test
    public void shouldUpdateUser() {
        User user = User.builder()
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        UserDto createdUser = userController.create(
                NewUserRequest.builder()
                        .name(user.getName())
                        .login(user.getLogin())
                        .email(user.getEmail())
                        .birthday(user.getBirthday())
                        .build()
        );

        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .name("kroki")
                .email(createdUser.getEmail())
                .login(createdUser.getLogin())
                .birthday(createdUser.getBirthday())
                .id(createdUser.getId())
                .build();

        UserDto updatedUserDto = userController.update(updateRequest);

        Assertions.assertNotNull(createdUser.getId(), "ID созданного пользователя не должен быть null");
        Assertions.assertEquals("kroki", updatedUserDto.getName(), "Имя пользователя должно быть обновлено");
        Assertions.assertEquals(createdUser.getId(), updatedUserDto.getId(), "ID пользователя должно остаться прежним");
    }

    @Test
    public void shouldGetAllUsers() {
        UserDto user1 = userController.create(NewUserRequest.builder()
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build());

        UserDto user2 = userController.create(NewUserRequest.builder()
                .name("AntonCa")
                .login("Anton")
                .email("anon.anton@yandex.ru")
                .birthday(birthday)
                .build());

        List<UserDto> usersList = userController.allListUsers();

        Assertions.assertEquals(2, usersList.size(), "Должно быть два пользователя");

        Assertions.assertTrue(usersList.contains(user1), "Список пользователей должен " +
                "содержать Smolcap");
        Assertions.assertTrue(usersList.contains(user2), "Список пользователей должен " +
                "содержать AntonCa");
    }

    @Test
    public void shouldntBirthdayToBeInAfter() {
        LocalDate invalidBirthday = LocalDate.of(2025, 11, 12);

        NewUserRequest request = NewUserRequest.builder()
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(invalidBirthday)
                .build();

        Assertions.assertThrows(ValidationException.class, () -> {
            userController.create(request);
        });
    }

    @Test
    public void shouldntEmailToBeEmptyAndNoHaveChar() {
        NewUserRequest newUserRequest = NewUserRequest.builder()
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smolyandex.ru")
                .birthday(birthday)
                .build();

        Assertions.assertThrows(ValidationException.class, () -> {
            userController.create(newUserRequest);
        });
    }

    @Test
    public void shouldntLoginToBeEmptyAndHaveSpaces() {
        NewUserRequest newUserRequest = NewUserRequest.builder()
                .name("Smolcap")
                .login("")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        Assertions.assertThrows(ValidationException.class, () -> {
            userController.create(newUserRequest);
        });
    }

    @Test
    public void shouldNameToBeEmptyButUseLogin() {
        NewUserRequest newUserRequest = NewUserRequest.builder()
                .name(" ")
                .login("daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        UserDto create = userController.create(newUserRequest);
        Assertions.assertEquals("daniel", create.getName(), "Если имя пустое в таком случае будет " +
                "использован логин");
    }

    @Test
    public void shouldAddFriend() {
        NewUserRequest newUserRequest = NewUserRequest.builder()
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        UserDto userControllerCreate = userController.create(newUserRequest);
        Long getIdUsr1 = userControllerCreate.getId();

        NewUserRequest newUserRequest2 = NewUserRequest.builder()
                .name("StepaCap")
                .login("Stemap")
                .email("dany.smo@yandex.ru")
                .birthday(birthday)
                .build();

        UserDto userControllerCreate2 = userController.create(newUserRequest2);
        Long getIdUser2 = userControllerCreate2.getId();

        Set<Long> setController = userController.addFriend(getIdUsr1, getIdUser2);

        Assertions.assertTrue(setController.contains(getIdUser2), "Список друзей должен содержать идентификатор нового друга");

        UserDto user1 = userController.findById(getIdUsr1);
        UserDto user2 = userController.findById(getIdUser2);

        Assertions.assertNotNull(user1, "Пользователь с ID " + getIdUsr1 + " не найден");
        Assertions.assertNotNull(user2, "Пользователь с ID " + getIdUser2 + " не найден");

        Assertions.assertTrue(user1.getFriends().contains(getIdUser2), "Пользователь с ID "
                + getIdUser2 + " должен находиться в друзьях у пользователя с ID " + getIdUsr1);
        Assertions.assertTrue(user2.getFriends().contains(getIdUsr1), "Пользователь с ID "
                + getIdUsr1 + " должен находиться в друзьях у пользователя с ID " + getIdUser2);
    }

    @Test
    public void shouldAddFriendAlreadyFriends() {
        NewUserRequest newUserRequest = NewUserRequest.builder()
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        UserDto userControllerCreate = userController.create(newUserRequest);
        Long getIdUsr1 = userControllerCreate.getId();

        NewUserRequest newUserRequest2 = NewUserRequest.builder()
                .name("StepaCap")
                .login("Stemap")
                .email("dany.smo@yandex.ru")
                .birthday(birthday)
                .build();

        UserDto userControllerCreate2 = userController.create(newUserRequest2);
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
        Assertions.assertEquals("Пользователь с ID: " + 1L + " или друг с ID " + 2L + " не найдены",
                exception.getMessage());
    }

    @Test
    public void notShouldRemoveFriendNotFound() {
        Exception exception = Assertions.assertThrows(NotFoundException.class, () -> {
            userController.deleteFriend(1L, 2L);
        });
        Assertions.assertEquals("Пользователь или друг с ID: " + 1L + " " + 2L + " не найдены",
                exception.getMessage());
    }

    @Test
    public void shouldGetMutualFriends() {
        NewUserRequest newUserRequest = NewUserRequest.builder()
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        UserDto userControllerCreate = userController.create(newUserRequest);
        Long getIdUsr1 = userControllerCreate.getId();

        NewUserRequest newUserRequest2 = NewUserRequest.builder()
                .name("StepaCap")
                .login("Stemap")
                .email("dany.smo@yandex.ru")
                .birthday(birthday)
                .build();

        UserDto userControllerCreate2 = userController.create(newUserRequest2);
        Long getIdUser2 = userControllerCreate2.getId();

        NewUserRequest newUserRequest3 = NewUserRequest.builder()
                .name("Genadii")
                .login("Stemap")
                .email("genka.smo@yandex.ru")
                .birthday(birthday)
                .build();

        UserDto userControllerCreate3 = userController.create(newUserRequest3);
        Long getIdUser3 = userControllerCreate3.getId();
        userController.addFriend(getIdUsr1, getIdUser3);
        userController.addFriend(getIdUser2, getIdUser3);

        List<UserDto> mutualFriend = userController.getListMutualFriend(getIdUsr1, getIdUser2);
        Assertions.assertEquals(1, mutualFriend.size(), "Пользователь с ID " + getIdUsr1 +
                " и ID " + getIdUser2 + " имеют одного общего друга");
    }

    @Test
    public void notShouldGetMutualFriendWhenUserNotFound() {
        Exception exception = Assertions.assertThrows(NotFoundException.class, () -> {
            List<UserDto> mutualFriend = userController.getListMutualFriend(1L, 2L);
            Assertions.assertTrue(mutualFriend.isEmpty(), "Список пользователь должен быть пуст");
        });
        Assertions.assertEquals("Один из пользователей с ID: " + 1L + " или " + 2L + " не найден",
                exception.getMessage());
    }

    @Test
    public void shouldDeleteFriend() {
        NewUserRequest newUserRequest = NewUserRequest.builder()
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        UserDto userControllerCreate = userController.create(newUserRequest);
        Long getIdUsr1 = userControllerCreate.getId();

        NewUserRequest newUserRequest2 = NewUserRequest.builder()
                .name("StepaCap")
                .login("Stemap")
                .email("dany.smo@yandex.ru")
                .birthday(birthday)
                .build();

        UserDto userControllerCreate2 = userController.create(newUserRequest2);
        Long getIdUser2 = userControllerCreate2.getId();

        userController.addFriend(getIdUsr1, getIdUser2);

        Set<Long> setController = userController.deleteFriend(getIdUsr1, getIdUser2);

        Assertions.assertFalse(setController.contains(getIdUser2), "Список друзей должен быть пуст");
    }

    @Test
    public void shouldGetListFriendsById() {
        NewUserRequest newUserRequest1 = NewUserRequest.builder()
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        UserDto user1Dto = userController.create(newUserRequest1);
        Long userId1 = user1Dto.getId();

        NewUserRequest newUserRequest2 = NewUserRequest.builder()
                .name("StepaCap")
                .login("Stemap")
                .email("dany.smo@yandex.ru")
                .birthday(birthday)
                .build();

        UserDto user2Dto = userController.create(newUserRequest2);
        Long userId2 = user2Dto.getId();

        userController.addFriend(userId1, userId2);

        List<UserDto> friendsList1 = userController.getFriends(userId1);
        List<UserDto> friendsList2 = userController.getFriends(userId2);

        Assertions.assertEquals(1, friendsList1.size(), "Список должен содержать одного друга");
        Assertions.assertTrue(friendsList1.stream().anyMatch(friend -> friend.getId().equals(userId2)),
                "Должен находиться пользователь с ID " + userId2);

        Assertions.assertEquals(1, friendsList2.size(), "Список должен содержать одного друга");
        Assertions.assertTrue(friendsList2.stream().anyMatch(friend -> friend.getId().equals(userId1)),
                "Должен находиться пользователь с ID " + userId1);

    }

    @Test
    public void shouldMutualListFriends() {
        NewUserRequest newUserRequest1 = NewUserRequest.builder()
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        UserDto user1Dto = userController.create(newUserRequest1);

        NewUserRequest newUserRequest2 = NewUserRequest.builder()
                .name("StepaCap")
                .login("Stemap")
                .email("dany.smo@yandex.ru")
                .birthday(birthday)
                .build();
        UserDto user2Dto = userController.create(newUserRequest2);

        NewUserRequest newUserRequest3 = NewUserRequest.builder()
                .name("StepaCap")
                .login("Stemap")
                .email("dany.smo@yandex.ru")
                .birthday(birthday)
                .build();

        UserDto user3Dto = userController.create(newUserRequest3);

        userController.addFriend(user1Dto.getId(), user2Dto.getId());
        userController.addFriend(user1Dto.getId(), user3Dto.getId());
        userController.addFriend(user2Dto.getId(), user3Dto.getId());

        List<UserDto> mutualFriends = userController.getListMutualFriend(user1Dto.getId(), user2Dto.getId());
        Assertions.assertTrue(mutualFriends.stream()
                        .anyMatch(friend -> friend.getId().equals(user3Dto.getId())),
                "Общим другом должен быть user3 с ID: " + user3Dto.getId());
    }

    @Test
    public void shouldDeleteUser() {
        NewUserRequest newUserRequest1 = NewUserRequest.builder()
                .name("Smolcap")
                .login("Daniel")
                .email("dany.smol@yandex.ru")
                .birthday(birthday)
                .build();

        UserDto create = userController.create(newUserRequest1);
        userController.deleteUserById(create.getId());

        Assertions.assertThrows(NotFoundException.class, () -> {
            userController.deleteUserById(create.getId());
        }, "Пользователь должен быть удалён");
    }

//    @Test
//    public void shouldCreateFilm() {
//        LocalDate localDate = LocalDate.of(1997, 12, 16);
//        NewFilmRequest newFilmRequest = NewFilmRequest.builder()
//                .name("Titanic")
//                .description("Description Long")
//                .releaseDate(localDate)
//                .mpa_id(defaultMpa)
//                .duration(190L)
//                .build();
//
//        FilmDto filmControllerCreate = filmController.create(newFilmRequest);
//
//        FilmDto expect = FilmMapper.mapToFilmDto(FilmMapper.mapToFilm(newFilmRequest));
//        expect.setId(filmControllerCreate.getId());
//
//        Assertions.assertEquals(expect, filmControllerCreate, "Фильмы должны быть одинаковы");
//    }
//
//    @Test
//    public void shouldUpdateFilm() {
//        LocalDate localDate = LocalDate.of(1997, 12, 16);
//        NewFilmRequest newFilmRequest = NewFilmRequest.builder()
//                .name("Titanic")
//                .description("Description Long")
//                .releaseDate(localDate)
//                .duration(190L)
//                .mpa_id(defaultMpa)
//                .build();
//        FilmDto filmDto = filmController.create(newFilmRequest);
//
//        UpdateFilmRequest update = UpdateFilmRequest.builder()
//                .name("Titanic and Lodka")
//                .description("Description Long")
//                .releaseDate(localDate)
//                .duration(190L)
//                .mpa(defaultMpa)
//                .id(filmDto.getId())
//                .build();
//
//        FilmDto expected = FilmDto.builder()
//                .id(filmDto.getId())
//                .name("Titanic and Lodka")
//                .description("Description Long")
//                .releaseDate(localDate)
//                .mpa(FilmMapper.mapToIdRating(defaultMpa.getId()))
//                .duration(190L)
//                .build();
//
//        FilmDto filmControllerUpdate = filmController.update(update);
//        Assertions.assertEquals(expected, filmControllerUpdate, "Фильм должен соответствовать обновлённому");
//    }
//
//    @Test
//    public void shouldGetAllFilms() {
//        LocalDate localDate = LocalDate.of(1997, 12, 16);
//        NewFilmRequest newFilmRequest = NewFilmRequest.builder()
//                .name("Titanic")
//                .description("Description Long")
//                .releaseDate(localDate)
//                .duration(190L)
//                .mpa_id(defaultMpa)
//                .build();
//        filmController.create(newFilmRequest);
//
//        List<FilmDto> listFilms = filmController.allListFilm();
//
//        Assertions.assertEquals(1, listFilms.size(), "Список должен содержать 1 фильм");
//    }
//
//    @Test
//    public void shouldntNameFilmToBeEmpty() {
//        LocalDate localDate = LocalDate.of(1997, 12, 16);
//        NewFilmRequest newFilmRequest = NewFilmRequest.builder()
//                .name("")
//                .description("Description Long")
//                .releaseDate(localDate)
//                .duration(190L)
//                .mpa_id(defaultMpa)
//                .build();
//
//        Assertions.assertThrows(ValidationException.class, () -> {
//            filmController.create(newFilmRequest);
//        });
//    }
//
//    @Test
//    public void shouldMaxLongDescription200Char() {
//        LocalDate localDate = LocalDate.of(1997, 12, 16);
//        Duration duration = Duration.ofMinutes(194);
//        NewFilmRequest newFilmRequest = NewFilmRequest.builder()
//                .name("Titanic")
//                .description("Слишком длинное описание, которое превышает допустимую длину ........................." +
//                        "...................." + ".........................................." +
//                        ".....................................................")
//                .releaseDate(localDate)
//                .duration(190L)
//                .mpa_id(defaultMpa)
//                .build();
//
//        Assertions.assertThrows(ValidationException.class, () -> {
//            filmController.create(newFilmRequest);
//        });
//    }
//
//    @Test
//    public void shouldReleaseDateNotBefore28December1985Year() {
//        LocalDate localDate = LocalDate.of(1885, 12, 16);
//        Duration duration = Duration.ofMinutes(194);
//        NewFilmRequest newFilmRequest = NewFilmRequest.builder()
//                .name("Titanic")
//                .description("Description Long")
//                .releaseDate(localDate)
//                .duration(190L)
//                .mpa_id(defaultMpa)
//                .build();
//
//        Assertions.assertThrows(ValidationException.class, () -> {
//            filmController.create(newFilmRequest);
//        });
//    }
//
//    @Test
//    public void shouldDurationFilmInPositive() {
//        LocalDate localDate = LocalDate.of(1997, 12, 16);
//        NewFilmRequest newFilmRequest = NewFilmRequest.builder()
//                .name("Titanic")
//                .description("Description Long")
//                .releaseDate(localDate)
//                .duration(-178L)
//                .mpa_id(defaultMpa)
//                .build();
//
//        Assertions.assertThrows(ValidationException.class, () -> {
//            filmController.create(newFilmRequest);
//        });
//    }
//
//    @Test
//    public void shouldAddLike() {
//        LocalDate localDate = LocalDate.of(1997, 12, 16);
//        NewUserRequest newUserRequest = NewUserRequest.builder()
//                .name("Smolcap")
//                .login("Daniel")
//                .email("dany.smol@yandex.ru")
//                .birthday(birthday)
//                .build();
//
//        UserDto userControllerCreate = userController.create(newUserRequest);
//
//        NewFilmRequest newFilmRequest = NewFilmRequest.builder()
//                .name("Titanic")
//                .description("Description Long")
//                .releaseDate(localDate)
//                .duration(180L)
//                .mpa_id(defaultMpa)
//                .build();
//        FilmDto filmDto = filmController.create(newFilmRequest);
//        Set<Long> updatedLikes = filmController.addLikeFilms(filmDto.getId(), userControllerCreate.getId());
//        System.out.println("Update likes: " + updatedLikes);
//
//        Assertions.assertTrue(updatedLikes.contains(userControllerCreate.getId()), "Лайк не был" +
//                " установлен для фильма.");
//    }
//
//    @Test
//    public void shouldDeleteLike() {
//        LocalDate localDate = LocalDate.of(1997, 12, 16);
//        NewUserRequest newUserRequest = NewUserRequest.builder()
//                .name("Smolcap")
//                .login("Daniel")
//                .email("dany.smol@yandex.ru")
//                .birthday(localDate)
//                .build();
//
//        UserDto userControllerCreate = userController.create(newUserRequest);
//
//        NewFilmRequest newFilmRequest = NewFilmRequest.builder()
//                .name("Titanic")
//                .description("Description Long")
//                .releaseDate(localDate)
//                .duration(180L)
//                .mpa_id(defaultMpa)
//                .build();
//        FilmDto filmDto = filmController.create(newFilmRequest);
//
//        filmController.addLikeFilms(filmDto.getId(), userControllerCreate.getId());
//
//        Optional<FilmDto> currentLike = Optional.ofNullable(filmService.findById(filmDto.getId()));
//
//        System.out.println("Current likes before deletion: " + currentLike);
//
//        Set<Long> updatedLikes = filmController.deleteLike(filmDto.getId(), userControllerCreate.getId());
//
//        Assertions.assertTrue(updatedLikes.isEmpty() || !updatedLikes.contains(userControllerCreate.getId()),
//                "Лайк не был удален для фильма");
//    }
//
//    @Test
//    public void shouldDeleteFilm() {
//        LocalDate localDate = LocalDate.of(1997, 12, 16);
//        NewFilmRequest newFilmRequest = NewFilmRequest.builder()
//                .name("Titanic")
//                .description("Description Long")
//                .releaseDate(localDate)
//                .duration(180L)
//                .mpa_id(defaultMpa)
//                .build();
//        FilmDto createFilm = filmController.create(newFilmRequest);
//        filmController.deleteFilmById(createFilm.getId());
//
//        Assertions.assertThrows(NotFoundException.class, () -> {
//            filmController.deleteFilmById(createFilm.getId());
//        }, "Фильм должен быть удалён с ID " + createFilm.getId());
//    }
//
//    @Test
//    public void shouldGetTop10Films() {
//        LocalDate localDate = LocalDate.of(1997, 12, 16);
//        NewFilmRequest newFilmRequest = NewFilmRequest.builder()
//                .name("Titanic")
//                .description("Description Long")
//                .releaseDate(localDate)
//                .duration(180L)
//                .likes(20)
//                .mpa_id(defaultMpa)
//                .build();
//        filmController.create(newFilmRequest);
//
//        NewFilmRequest newFilmRequest2 = NewFilmRequest.builder()
//                .name("Robokop")
//                .description("Description Long")
//                .releaseDate(localDate)
//                .duration(180L)
//                .likes(10)
//                .mpa_id(defaultMpa)
//                .build();
//        filmController.create(newFilmRequest2);
//
//        NewFilmRequest newFilmRequest3 = NewFilmRequest.builder()
//                .name("Interstellar")
//                .description("Description Long")
//                .releaseDate(localDate)
//                .duration(180L)
//                .likes(30)
//                .mpa_id(defaultMpa)
//                .build();
//        filmController.create(newFilmRequest3);
//        List<FilmDto> popularFilm = filmController.topFilms(10);
//
//        Assertions.assertEquals(3, popularFilm.size(), "Количество фильмов должно быть 3");
//        Assertions.assertEquals("Interstellar", popularFilm.get(0).getName());
//        Assertions.assertEquals("Titanic", popularFilm.get(1).getName());
//        Assertions.assertEquals("Robokop", popularFilm.get(2).getName());
//    }
}


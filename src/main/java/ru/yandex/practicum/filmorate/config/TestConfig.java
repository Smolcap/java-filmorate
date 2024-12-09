package ru.yandex.practicum.filmorate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.FriendshipService;
import ru.yandex.practicum.filmorate.service.LikeService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.Like.InMemoryLikeStorage;
import ru.yandex.practicum.filmorate.storage.Like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.friend.FriendStorage;
import ru.yandex.practicum.filmorate.storage.friend.InMemoryFriend;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@Configuration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public UserStorage inMemoryUserStorage() {
        return new InMemoryUserStorage();
    }


    @Bean
    @Primary
    public FilmStorage inMemoryFilmStorage() {
        return new InMemoryFilmStorage();
    }

    @Bean
    @Primary
    public LikeStorage inMemoryLike() {
        return new InMemoryLikeStorage(inMemoryFilmStorage(), inMemoryUserStorage());
    }

    @Bean
    @Primary
    public FriendStorage inMemoryFriendStorage() {
        return new InMemoryFriend(inMemoryUserStorage());
    }

    @Bean
    public FilmService filmService() {
        return new FilmService(inMemoryFilmStorage());
    }

    @Bean
    public LikeService likeService() {
        return new LikeService(inMemoryLike());
    }

    @Bean
    public FriendshipService friendshipService() {
        return new FriendshipService(inMemoryFriendStorage());
    }

    @Bean
    public UserService userService() {
        return new UserService(inMemoryUserStorage());
    }
}

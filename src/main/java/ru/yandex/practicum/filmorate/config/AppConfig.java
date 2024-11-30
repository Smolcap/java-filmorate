package ru.yandex.practicum.filmorate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.Like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.friend.FriendStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@Configuration
@Profile("!test")
public class AppConfig {

    @Bean
    @Primary
    public FriendStorage friendDbStorage(UserDbStorage userDbStorage) {
        return userDbStorage;
    }

    @Bean
    @Primary
    public LikeStorage likeDbStorage(FilmDbStorage filmDbStorage) {
        return filmDbStorage;
    }

    @Bean(name = "filmDbStorageBean")
    @Primary
    public FilmStorage filmDbStorage(FilmDbStorage filmDbStorage) {
        return filmDbStorage;
    }

    @Bean(name = "userStorage")
    @Primary
    public UserStorage userDbStorage(UserDbStorage userDbStorage) {
        return userDbStorage;
    }
}

package ru.yandex.practicum.filmorate.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.constants.Status;
import ru.yandex.practicum.filmorate.dao.mapping.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friend.FriendStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository("userDbStorage")
public class UserDbStorage extends BaseRepository<User> implements UserStorage, FriendStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM user_app";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM user_app WHERE user_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO user_app (name, email, login, birthday)" +
            " VALUES (?, ?, ?, ?)";
    private static final String UPDATE_USER_QUERY = "UPDATE user_app SET name = ?, email = ?, login = ?, birthday = ? " +
            "WHERE user_id = ?";
    private static final String DELETE_USER_BY_ID_QUERY = "DELETE FROM user_app WHERE user_id = ?";
    private static final String DELETE_USERS_QUERY = "DELETE FROM user_app";
    private static final String INSERT_ADD_FRIEND_QUERY = "INSERT INTO friend_user (user_id, friend_id, status_friend) " +
            "VALUES(?, ?, ?)";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friend_user WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_RECORD_FRIEND = "SELECT status_friend FROM friend_user " +
            "WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_ALL_FRIENDS_QUERY = "SELECT friend_id FROM friend_user WHERE user_id = ?";
    private static final String FIND_MUTUAL_FRIEND = "SELECT friend_id FROM friend_user WHERE user_id = ? AND friend_id " +
            "IN (SELECT friend_id FROM friend_user WHERE user_id = ?)";

    private static final Logger logger = LoggerFactory.getLogger(UserDbStorage.class);

    @Autowired
    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public User create(User user) {
        Long id = insert(INSERT_QUERY,
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getBirthday()
        );
        user.setId(id);
        logger.info("Создание пользователя с ID: {} ", id);
        return user;
    }

    @Override
    public User update(User newUser) {
        try {
            update(
                    UPDATE_USER_QUERY,
                    newUser.getName(),
                    newUser.getEmail(),
                    newUser.getLogin(),
                    newUser.getBirthday(),
                    newUser.getId()
            );
            return newUser;
        } catch (NotFoundException e) {
            logger.error("Пользователь с таким Id не найден: {}", newUser.getId());
            throw new NotFoundException("Пользователь с таким Id не найден");
        }
    }

    @Override
    public List<User> getAllUsers() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return findOne(FIND_BY_ID_QUERY, userId);
    }

    @Override
    public boolean deleteUserById(Long userId) {
        return delete(DELETE_USER_BY_ID_QUERY, userId);
    }

    @Override
    public void clearUsers() {
        update(DELETE_USERS_QUERY);
    }

    @Override
    public Set<Long> addFriends(Long userId, Long friendId) {

        try {
            logger.info("Добавление друга: userId = {}, friendId = {}", userId, friendId);

            String statusAddFriend = jdbc.queryForList(FIND_RECORD_FRIEND, String.class, userId, friendId).stream()
                    .findFirst()
                    .map(status -> {
                        if (status.equals(Status.FRIEND.name()) || status.equals(Status.ACCEPTED.name())) {
                            logger.warn("Попытка добавить уже существующего друга: userId = {}, friendId = {}", userId, friendId);
                            throw new ValidationException("Нельзя добавить в друзья уже существующего друга");
                        }
                        return status;
                    })
                    .orElse(null);

            jdbc.update(INSERT_ADD_FRIEND_QUERY, userId, friendId, Status.UNCONFIRMED.name());
            logger.info("Друг успешно добавлен: userId = {}, friendId = {}", userId, friendId);
            return Set.of(friendId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Добавление в друзья не существующего пользователя");
        }
    }

    @Override
    public Set<Long> deleteFromFriends(Long userId, Long friendId) {
        logger.info("Удаление друга: userId = {}, friendId = {}", userId, friendId);

        String statusDeleteFriend = jdbc.queryForList(FIND_RECORD_FRIEND, String.class, userId, friendId).stream()
                .findFirst()
                .map(status -> {
                    if (!status.equals(Status.UNCONFIRMED.name())) {
                        logger.warn("Попытка удалить подтверждённую дружбу: userId = {}, friendId = {}", userId, friendId);
                        throw new ValidationException("Нельзя удалить дружбу, если она подтверждена.");
                    }
                    return status;
                })
                .orElseThrow(() -> {
                    logger.warn("Дружба не найдена для userId = {}, friendId = {}", userId, friendId);
                    return new NotFoundException("Дружба не найдена");
                });

        jdbc.update(DELETE_FRIEND_QUERY, userId, friendId);
        logger.info("Друг успешно удалён: userId = {}, friendId = {}", userId, friendId);
        return Set.of(friendId);
    }

    @Override
    public List<User> findAllFriend(Long userId) {
        logger.info("Поиск всех друзей для userId = {}", userId);

        List<Long> friendsId = jdbc.query(
                FIND_ALL_FRIENDS_QUERY,
                (rs, rowNum) -> rs.getLong("friend_id"),
                userId
        );

        logger.info("Найдены ID друзей: {}", friendsId);

        List<User> friends = friendsId.stream()
                .map(this::findById)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        logger.info("Возвращаем друзей для userId = {}: {}", userId, friends);
        return friends;
    }

    @Override
    public List<User> listMutualFriend(Long userId, Long friendId) {
        logger.info("Поиск общих друзей: userId = {}, friendId = {}", userId, friendId);


        List<Long> mutualFriendIds = jdbc.query(
                FIND_MUTUAL_FRIEND,
                (rs, rowNum) -> rs.getLong("friend_id"),
                userId, friendId
        );

        logger.info("ID общих друзей: {}", mutualFriendIds);

        List<User> mutualFriends = mutualFriendIds.stream()
                .distinct() // Убедитесь, что идентификаторы уникальны
                .map(this::findById)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        logger.info("Возвращаем общих друзей для userId = {}, friendId = {}: {}", userId, friendId, mutualFriends);
        return mutualFriends;
    }
}

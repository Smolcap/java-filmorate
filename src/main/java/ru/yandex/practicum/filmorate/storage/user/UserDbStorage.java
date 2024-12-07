package ru.yandex.practicum.filmorate.storage.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.constants.Status;
import ru.yandex.practicum.filmorate.dao.BaseRepository;
import ru.yandex.practicum.filmorate.dao.mapping.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friend.FriendStorage;

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
    private static final String UPDATE_STATUS_FRIEND_QUERY = "UPDATE friend_user SET status_friend " +
            "WHERE user_id = ? AND friend_id = ?";
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
        logger.info("Создание пользователя: {}", user);
        Long id = addUser(user);
        user.setId(id);
        logger.debug("Создание пользователя с ID: {} ", id);
        return user;
    }

    @Override
    public User update(User newUser) {
        logger.info("Обновление пользователя с ID: {}, Имя: {}, Email: {}", newUser.getId(), newUser.getName(),
                newUser.getEmail());

        Optional<User> existing = findById(newUser.getId());
        if (existing.isEmpty()) {
            logger.warn("Пользователь не найден с ID: {}", newUser.getId());
            throw new NotFoundException("Пользователь с таким Id не найден");
        }

        updateUser(newUser);
        return newUser;
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
        logger.info("Добавление друга: userId = {}, friendId = {}", userId, friendId);

        Optional<User> friend = findOne("SELECT * FROM user_app WHERE user_id = ?", friendId);
        if (friend.isEmpty()) {
            logger.warn("Попытка добавить несуществующего пользователя с ID: {}", friendId);
            throw new NotFoundException("Пользователь с таким ID не существует.");
        }

        List<String> getStatusFriend = jdbc.queryForList(FIND_RECORD_FRIEND, String.class, userId, friendId);
        if (getStatusFriend.isEmpty()) {
            jdbc.update(INSERT_ADD_FRIEND_QUERY, userId, friendId, Status.UNCONFIRMED.name());
            logger.info("Запрос в друзья успешно отправлен: userId = {}, friendId = {}", userId, friendId);
            return Set.of(friendId);
        }

        String statusFriend = getStatusFriend.get(1);
        if (Status.UNCONFIRMED.name().equals(statusFriend)) {
            confirmFriendship(userId, friendId);
            return Set.of(friendId);
        }

        if (Status.FRIEND.name().equals(getStatusFriend.get(0)) || Status.NONE.name().equals(getStatusFriend.get(2))) {
            logger.warn("Невозможно добавить друга: userId = {}, friendId = {}. Статус: {}", userId, friendId, getStatusFriend);
            throw new ValidationException("Нельзя добавить в друзья уже существующего или несуществующего друга.");
        }

        jdbc.update(INSERT_ADD_FRIEND_QUERY, userId, friendId, Status.UNCONFIRMED.name());
        logger.info("Запрос в друзья успешно отправлен: userId = {}, friendId = {}", userId, friendId);
        return Set.of(friendId);
    }

    @Override
    public Set<Long> deleteFromFriends(Long userId, Long friendId) {
        logger.info("Удаление друга: userId = {}, friendId = {}", userId, friendId);

        String getStatusFriendDelete = jdbc.queryForObject(FIND_RECORD_FRIEND, String.class, userId, friendId);

        if (getStatusFriendDelete == null) {
            logger.warn("Дружба не найдена для userId = {}, friendId = {}", userId, friendId);
            throw new NotFoundException("Нельзя удалить не подтверждённую дружбу");
        }
        if (getStatusFriendDelete.equals(Status.UNCONFIRMED.name()) || getStatusFriendDelete.equals(Status.FRIEND.name())) {
            logger.info("Запрос на дружбу успешно удален: userId = {}, friendId = {}", userId, friendId);
            jdbc.update(DELETE_FRIEND_QUERY, userId, friendId);
            return Set.of(friendId);
        } else {
            logger.warn("Невозможно удалить дружбу с userId = {}, friendId = {}. Статус: {}", userId, friendId, getStatusFriendDelete);
            throw new ValidationException("Нельзя удалить дружбу в статусе неподтвержденной");
        }

//        String statusDeleteFriend = jdbc.queryForList(FIND_RECORD_FRIEND, String.class, userId, friendId).stream()
//                .findFirst()
//                .map(status -> {
//                    if (!status.equals(Status.UNCONFIRMED.name())) {
//                        logger.warn("Попытка удалить подтверждённую дружбу: userId = {}, friendId = {}", userId, friendId);
//                        throw new ValidationException("Нельзя удалить дружбу, если она подтверждена.");
//                    }
//                    return status;
//                })
//                .orElseThrow(() -> {
//                    logger.warn("Дружба не найдена для userId = {}, friendId = {}", userId, friendId);
//                    return new NotFoundException("Дружба не найдена");
//                });
//
//        jdbc.update(DELETE_FRIEND_QUERY, userId, friendId);
//        logger.info("Друг успешно удалён: userId = {}, friendId = {}", userId, friendId);
//        return Set.of(friendId);
    }

    @Override
    public List<User> findAllFriend(Long userId) {
        logger.info("Поиск всех друзей для userId = {}", userId);

        Optional<User> haveUser = findById(userId);
        if (haveUser.isEmpty()) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }

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

    private Long addUser(User user) {
        return insert(INSERT_QUERY,
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getBirthday()
        );
    }

    private void updateUser(User newUser) {
        update(
                UPDATE_USER_QUERY,
                newUser.getName(),
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getBirthday(),
                newUser.getId()
        );
    }

    private void confirmFriendship(Long userId, Long friendId) {
        logger.info("Подтверждение дружбы: userId = {}, friendId = {}", userId, friendId);

        String status = jdbc.queryForObject(FIND_RECORD_FRIEND, String.class, userId, friendId);

        if (status == null) {
            logger.warn("Не найден запрос на дружбу для userId = {}, friendId = {}", userId, friendId);
            throw new NotFoundException("Запрос на дружбу не найден");
        }

        if (!status.equals(Status.UNCONFIRMED.name())) {
            logger.warn("Невозможно подтвердить дружбу для userId = {}, friendId = {}: неверный статус дружбы: {}", userId, friendId, status);
            throw new ValidationException("Невозможно подтвердить дружбу, так как статус не UNCONFIRMED");
        }

        jdbc.update(UPDATE_STATUS_FRIEND_QUERY, Status.FRIEND.name(), userId, friendId);
        logger.info("Дружба подтверждена: userId = {}, friendId = {}", userId, friendId);
    }
}

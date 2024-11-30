package ru.yandex.practicum.filmorate.dao.mapping;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet resultSet, int rowNum) throws SQLException {
//        Long userId = resultSet.getLong("user_id");
//        Long friendId = resultSet.getLong("friend_id");

        User user = User.builder()
                .id(resultSet.getLong("user_id"))
                .name(resultSet.getString("name"))
                .email(resultSet.getString("email"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .login(resultSet.getString("login"))
                .build();

        return user;
    }

//    private Set<Long> getFriends(Long userId) {
//        String query = "SELECT friend_id FROM friend_user WHERE user_id = ?";
//
//        return new HashSet<>(jdbc.query(query, new Object[]{userId}, (rs, rowNum) ->
//                rs.getLong("friend_id")));
//    }
//
//    private Optional<Status> getStatusFriend(Long userId, Long friendId) {
//        String query = "SELECT status_friend FROM friend_user WHERE user_id = ? AND friend_id = ?";
//
//        try {
//            return Optional.ofNullable(jdbc.queryForObject(query, new Object[]{userId, friendId}, String.class))
//                    .map(Status::valueOf);
//        } catch (EmptyResultDataAccessException e) {
//            return Optional.empty();
//        }
//    }
}

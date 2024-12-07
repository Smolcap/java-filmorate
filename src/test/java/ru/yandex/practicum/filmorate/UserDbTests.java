package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.mapping.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
public class UserDbTests {

    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.execute("DELETE FROM user_app");
    }

    @Test
    public void shouldSaveUserInDb() {
        User user = userStorage.create(User.builder()
                .name("User1")
                .email("user1@example.com")
                .login("testlon1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        assertThat(user).isNotNull();

        assertThat(user.getName()).isEqualTo("User1");
        assertThat(user.getEmail()).isEqualTo("user1@example.com");
        assertThat(user.getLogin()).isEqualTo("testlon1");
        assertThat(user.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));

        assertThat(user.getId()).isNotNull();
    }

    @Test
    public void shouldUpdateUserInDb() {
        User user = userStorage.create(User.builder()
                .name("User1")
                .email("user1@example.com")
                .login("testlogin1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        user.setName("Smolcap");

        User updatedUser = userStorage.update(user);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getName()).isEqualTo("Smolcap");
        assertThat(updatedUser.getEmail()).isEqualTo("user1@example.com");
        assertThat(updatedUser.getLogin()).isEqualTo("testlogin1");
        assertThat(updatedUser.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(updatedUser.getId()).isNotNull();

    }

    @Test
    public void testGetAllUserDb() {
        userStorage.create(User.builder()
                .name("User1")
                .email("user1@example.com")
                .login("testlogin1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        List<User> users = userStorage.getAllUsers();

        assertThat(users).hasSize(1);
        assertThat(users).extracting("name")
                .containsExactlyInAnyOrder("User1");
    }

    @Test
    public void shouldFindByIdUserDb() {
        User user = userStorage.create(User.builder()
                .name("User1")
                .email("user1@example.com")
                .login("testlogin1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        Optional<User> userFind = userStorage.findById(user.getId());

        assertThat(userFind).isNotNull();
        assertThat(userFind.get().getName()).isEqualTo("User1");
    }

    @Test
    public void shouldDeleteUserById() {
        User user = userStorage.create(User.builder()
                .name("User1")
                .email("user1@example.com")
                .login("testlogin1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        assertThat(userStorage.findById(user.getId())).isPresent();

        boolean deletedUser = userStorage.deleteUserById(user.getId());

        assertThat(deletedUser).isTrue();
    }

    @Test
    public void shouldClearUsersDb() {
        User user = userStorage.create(User.builder()
                .name("User1")
                .email("user1@example.com")
                .login("testlogin1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());
        userStorage.clearUsers();
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_app", Integer.class);
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void shouldAddFriendDb() {
        User user1 = userStorage.create(User.builder()
                .name("User1")
                .email("user1@example.com")
                .login("testlogin1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        User user2 = userStorage.create(User.builder()
                .name("Use2")
                .email("use1@example.com")
                .login("tewtlogin1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        Set<Long> addFriend = userStorage.addFriends(user1.getId(), user2.getId());
        assertThat(addFriend).isNotNull();

        List<User> friendsOfUser1 = userStorage.findAllFriend(user1.getId());

        assertThat(friendsOfUser1).contains(user2);
    }

    @Test
    public void shouldDeleteFriendDb() {
        User user1 = userStorage.create(User.builder()
                .name("User1")
                .email("user1@example.com")
                .login("testlogin1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        User user2 = userStorage.create(User.builder()
                .name("Use2")
                .email("use1@example.com")
                .login("tewtlogin1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        Set<Long> addFriend = userStorage.addFriends(user1.getId(), user2.getId());
        assertThat(addFriend).isNotNull();
        Set<Long> addFriend2 = userStorage.addFriends(user2.getId(), user1.getId());
        assertThat(addFriend2).isNotNull();

        Set<Long> deleteFriend = userStorage.deleteFromFriends(user1.getId(), user2.getId());

        assertThat(deleteFriend).isNotNull();

        List<User> friendsOfUser1 = userStorage.findAllFriend(user1.getId());
        List<User> friendsOfUser2 = userStorage.findAllFriend(user2.getId());

        assertThat(friendsOfUser1).doesNotContain(user2);
        assertThat(friendsOfUser2).contains(user1);
    }

    @Test
    public void shouldGetAllFriend() {
        User user1 = userStorage.create(User.builder()
                .name("User1")
                .email("user1@example.com")
                .login("testlogin1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        User user2 = userStorage.create(User.builder()
                .name("Use2")
                .email("use1@example.com")
                .login("tewtlogin1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        userStorage.addFriends(user1.getId(), user2.getId());

        User user3 = userStorage.create(User.builder()
                .name("Use2")
                .email("use32e1@example.com")
                .login("tewtlogin1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());
        userStorage.addFriends(user1.getId(), user3.getId());

        List<User> getAllFriend = userStorage.findAllFriend(user1.getId());

        assertThat(getAllFriend).hasSize(2);
    }

    @Test
    public void shouldGetMutualFriendDb() {
        User user1 = userStorage.create(User.builder()
                .name("User1")
                .email("user1@example.com")
                .login("testlogin1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        User user2 = userStorage.create(User.builder()
                .name("Use2")
                .email("use1@examp.com")
                .login("tewtlogin1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());


        User user3 = userStorage.create(User.builder()
                .name("Use2")
                .email("use32e1@example.com")
                .login("tewtlogin1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());
        userStorage.addFriends(user1.getId(), user2.getId());
        userStorage.addFriends(user1.getId(), user3.getId());
        userStorage.addFriends(user2.getId(), user3.getId());

        List<User> mutualFriends = userStorage.listMutualFriend(user1.getId(), user2.getId());
        List<User> expectedMutualFriends = List.of(user3);

        assertThat(mutualFriends).containsExactlyInAnyOrderElementsOf(expectedMutualFriends);
    }

    @Test
    public void shouldNotDeleteNonExistingFriend() {
        User user1 = userStorage.create(User.builder()
                .name("User1")
                .email("user1@example.com")
                .login("testlogin1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        User user2 = userStorage.create(User.builder()
                .name("User2")
                .email("user2@example.com")
                .login("testlogin2")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        Set<Long> deleteNotFriend = userStorage.deleteFromFriends(user1.getId(), user2.getId());

        assertThat(deleteNotFriend).contains(user2.getId());

        List<User> friendsOfUser1 = userStorage.findAllFriend(user1.getId());
        assertThat(friendsOfUser1).isEmpty();

        List<User> friendsOfUser2 = userStorage.findAllFriend(user2.getId());
        assertThat(friendsOfUser2).isEmpty();
    }
}

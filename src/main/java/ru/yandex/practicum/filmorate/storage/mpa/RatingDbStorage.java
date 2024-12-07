package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.BaseRepository;
import ru.yandex.practicum.filmorate.dao.mapping.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
public class RatingDbStorage extends BaseRepository<Mpa> implements RatingStorage {

    @Autowired
    public RatingDbStorage(JdbcTemplate jdbc, MpaRowMapper mapper) {
        super(jdbc, mapper);
    }

    private static final String FIND_ALL_MPA = "SELECT * FROM rating";
    private static final String FIND_MPA_BY_ID = "SELECT * FROM rating WHERE rating_id = ?";

    @Override
    public List<Mpa> getAllRatings() {
        return findMany(FIND_ALL_MPA);
    }

    @Override
    public Optional<Mpa> getRatingById(int mpaId) {
        return findOne(FIND_MPA_BY_ID, mpaId);
    }
}

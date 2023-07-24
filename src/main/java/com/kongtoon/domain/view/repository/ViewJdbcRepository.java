package com.kongtoon.domain.view.repository;

import com.kongtoon.domain.view.model.View;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ViewJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void batchInsert(List<View> views) {
        String sql = "INSERT INTO view (user_id, episode_id, first_access_time, last_access_time) VALUES (?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(
                sql,
                views,
                views.size(),
                (PreparedStatement ps, View view) -> {
                    ps.setLong(1, view.getUser().getId());
                    ps.setLong(2, view.getEpisode().getId());
                    ps.setTimestamp(3, Timestamp.valueOf(view.getFirstAccessTime()));
                    ps.setTimestamp(4, Timestamp.valueOf(view.getLastAccessTime()));
                }
        );
    }

    @Transactional
    public void batchUpdate(List<View> viewCandidates) {
        String sql = "INSERT INTO view (id, user_id, episode_id, first_access_time, last_access_time) " +
                "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE last_access_time = ?";

        jdbcTemplate.batchUpdate(
                sql,
                viewCandidates,
                viewCandidates.size(),
                (PreparedStatement ps, View view) -> {
                    ps.setLong(1, view.getId());
                    ps.setLong(2, view.getUser().getId());
                    ps.setLong(3, view.getEpisode().getId());
                    ps.setTimestamp(4, Timestamp.valueOf(view.getFirstAccessTime()));
                    ps.setTimestamp(5, Timestamp.valueOf(view.getLastAccessTime()));
                    ps.setTimestamp(6, Timestamp.valueOf(view.getLastAccessTime()));
                }
        );
    }
}
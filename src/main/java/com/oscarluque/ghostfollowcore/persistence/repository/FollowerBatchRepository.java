package com.oscarluque.ghostfollowcore.persistence.repository;

import com.oscarluque.ghostfollowcore.dto.follower.InstagramProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FollowerBatchRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String SQL_DELETE_BY_ACCOUNT = """
        DELETE FROM account_followers 
        WHERE account_id = ?
        """;

    private static final String SQL_INSERT_BATCH = """
        INSERT INTO account_followers (
            account_id, 
            follower_username, 
            follower_profile_url, 
            last_update
        ) VALUES (?, ?, ?, ?)
        """;

    private static final int BATCH_SIZE = 1000;

    public void deleteAllByAccountId(Integer accountId) {
        jdbcTemplate.update(SQL_DELETE_BY_ACCOUNT, accountId);
    }

    public void saveAllInBatch(List<InstagramProfile> followers, Integer accountId) {
        if (followers.isEmpty()) return;

        long start = System.currentTimeMillis();

        jdbcTemplate.batchUpdate(
                SQL_INSERT_BATCH,
                followers,
                BATCH_SIZE,
                (PreparedStatement ps, InstagramProfile profile) -> {
                    ps.setInt(1, accountId);
                    ps.setString(2, profile.getValue());
                    ps.setString(3, profile.getHref());
                    ps.setTimestamp(4, new Timestamp(profile.getTimestamp() * 1000L));
                });

        log.info("🚀 JDBC: {} seguidores insertados en {} ms", followers.size(), (System.currentTimeMillis() - start));
    }
}
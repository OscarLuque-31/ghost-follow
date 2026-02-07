package com.oscarluque.ghostfollowcore.persistence.repository;

import com.oscarluque.ghostfollowcore.persistence.entity.FollowerDetail;
import com.oscarluque.ghostfollowcore.persistence.entity.FollowerId;
import com.oscarluque.ghostfollowcore.persistence.entity.FollowingDetail;
import com.oscarluque.ghostfollowcore.persistence.entity.FollowingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountFollowingRepository extends JpaRepository<FollowingDetail, FollowingId> {

    Optional<List<FollowingDetail>> findById_AccountId(Integer accountId);

    @Modifying
    @Query("DELETE FROM FollowingDetail f WHERE f.id.accountId = :accountId")
    void deleteByAccountId(@Param("accountId") Integer accountId);

}

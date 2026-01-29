package com.oscarluque.ghostfollowcore.persistence.repository;

import com.oscarluque.ghostfollowcore.persistence.entity.FollowerDetail;
import com.oscarluque.ghostfollowcore.persistence.entity.MonitoredAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountFollowersRepository extends JpaRepository<FollowerDetail, Integer> {

    Optional<List<FollowerDetail>> findByAccountId(Integer accountId);

}

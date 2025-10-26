package com.ghostfollow.processor_service.repository;

import com.ghostfollow.processor_service.entity.AccountFollowerState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowerStateRepository extends JpaRepository<AccountFollowerState, String> {
}

package com.oscarluque.ghostfollowcore.persistence.repository;

import com.oscarluque.ghostfollowcore.persistence.entity.MonitoredAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<MonitoredAccount, Integer> {

    Optional<MonitoredAccount> findByUserEmail(String email);

}

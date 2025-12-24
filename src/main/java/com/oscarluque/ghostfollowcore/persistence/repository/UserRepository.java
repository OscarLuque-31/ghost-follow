package com.oscarluque.ghostfollowcore.persistence.repository;

import com.oscarluque.ghostfollowcore.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByEmail(String email);
}

package com.oscarluque.ghostfollowcore.persistence.repository;

import com.oscarluque.ghostfollowcore.persistence.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    Optional<Subscription> findByStripeCustomerId(String stripeCustomerId);
    
    Optional<Subscription> findByUserId(Long userId);
}
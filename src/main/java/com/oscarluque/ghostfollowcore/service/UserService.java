package com.oscarluque.ghostfollowcore.service;

import com.oscarluque.ghostfollowcore.constants.PlanType;
import com.oscarluque.ghostfollowcore.constants.SubscriptionStatus;
import com.oscarluque.ghostfollowcore.persistence.entity.Subscription;
import com.oscarluque.ghostfollowcore.persistence.entity.User;
import com.oscarluque.ghostfollowcore.persistence.repository.SubscriptionRepository;
import com.oscarluque.ghostfollowcore.persistence.repository.UserRepository;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public void upgradeUserToPremium(Session session){

        String email = session.getCustomerDetails() != null ?
                session.getCustomerDetails().getEmail() : session.getCustomerEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Subscription subscription = subscriptionRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("No existe subcripcion"));

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setStripeCustomerId(session.getCustomer());

        String modeSubscription = "subscription";
        String modePayment = "payment";

        if (modePayment.equals(session.getMode())) {
            subscription.setPlanType(PlanType.PREMIUM_LIFETIME);
            subscription.setStripeSubscriptionId(null);
            subscription.setCurrentPeriodEnd(null);
        } else if (modeSubscription.equals(session.getMode())) {
            subscription.setPlanType(PlanType.PREMIUM_MONTHLY);
            subscription.setStripeSubscriptionId(session.getSubscription());
            subscription.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));
        }

        subscriptionRepository.save(subscription);
        log.info("¡Usuario {} actualizado a PREMIUM con éxito!", email);
    }

    @Transactional
    public void handleSubscriptionUpdate(com.stripe.model.Subscription stripeSubscription) {
        Subscription localSubscription = subscriptionRepository.findByStripeSubscriptionId(stripeSubscription.getId())
                .orElseThrow(() -> new IllegalArgumentException("No existe subcripcion"));

        String stripeStatus = stripeSubscription.getStatus();

        boolean cancelAtPeriodEnd = stripeSubscription.getCancelAtPeriodEnd();

        String subscriptionCanceled = "canceled";
        String subscriptionUnpaid = "unpaid";
        String subscriptionActive = "active";

        if (subscriptionCanceled.equals(stripeStatus) || subscriptionUnpaid.equals(stripeStatus)) {
            localSubscription.setStatus(SubscriptionStatus.EXPIRED);
            localSubscription.setPlanType(PlanType.FREE);

        } else if (subscriptionActive.equals(stripeStatus) && cancelAtPeriodEnd) {
            localSubscription.setStatus(SubscriptionStatus.CANCELED);

        } else if (subscriptionActive.equals(stripeStatus)) {
            localSubscription.setStatus(SubscriptionStatus.ACTIVE);
            localSubscription.setPlanType(PlanType.PREMIUM_MONTHLY);
        }

        subscriptionRepository.save(localSubscription);
        log.info("Suscripción de Stripe ({}) actualizada al estado: {}", stripeSubscription.getId(), localSubscription.getStatus());
    }
}

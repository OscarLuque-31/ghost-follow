package com.oscarluque.ghostfollowcore.web;

import com.oscarluque.ghostfollowcore.service.UserService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final UserService userService;

    @Value("${STRIPE_WEBHOOK_SECRET}")
    private String endpointSecret;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeEvent(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) throws SignatureVerificationException {

        Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        String eventType = event.getType();

        String checkoutCompletedEvent = "checkout.session.completed";
        String subscriptionUpdatedEvent = "customer.subscription.updated";
        String subscriptionDeletedEvent = "customer.subscription.deleted";


        log.info("Received Stripe webhook event: {}", eventType);

        if (checkoutCompletedEvent.equals(eventType)) {
            Optional<StripeObject> stripeObject = event.getDataObjectDeserializer().getObject();

            if (stripeObject.isPresent() && stripeObject.get() instanceof Session session) {
                log.info("Processing checkout completed for session: {}", session.getId());
                userService.upgradeUserToPremium(session);
                log.info("User upgraded to premium successfully. Session: {}", session.getId());
            } else {
                log.warn("Failed to deserialize Stripe session object for event: {}", event.getId());
            }

        } else if (subscriptionUpdatedEvent.equals(eventType) || subscriptionDeletedEvent.equals(eventType)) {
            Optional<StripeObject> stripeObject = event.getDataObjectDeserializer().getObject();

            if (stripeObject.isPresent() && stripeObject.get() instanceof Subscription subscription) {
                log.info("Processing subscription update. Event: {}, Subscription: {}", eventType, subscription.getId());
                userService.handleSubscriptionUpdate(subscription);
                log.info("Subscription state updated successfully. Subscription: {}", subscription.getId());
            } else {
                log.warn("Failed to deserialize Stripe subscription object for event: {}", event.getId());
            }
        } else {
            log.info("Unhandled Stripe event type: {}", eventType);
        }

        return ResponseEntity.ok("Received");
    }
}
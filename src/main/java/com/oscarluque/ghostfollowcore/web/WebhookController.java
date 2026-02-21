package com.oscarluque.ghostfollowcore.web;

import com.oscarluque.ghostfollowcore.service.UserService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final UserService userService;

    @Value("${STRIPE_WEBHOOK_SECRET}")
    private String endpoindSecret;


    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeEvent(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) throws SignatureVerificationException {

        Event event;
        event = Webhook.constructEvent(payload, sigHeader, endpoindSecret);
        String checkoutCompletedEvent = "checkout.session.completed";

        if (checkoutCompletedEvent.equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().get();
            userService.upgradeUserToPremium(session);
        }

        return ResponseEntity.ok("Recibido");
    }

}

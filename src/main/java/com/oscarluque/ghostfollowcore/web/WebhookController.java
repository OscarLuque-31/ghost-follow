package com.oscarluque.ghostfollowcore.web;

import com.oscarluque.ghostfollowcore.service.UserService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final UserService userService;

    @Value("${STRIPE_WEBHOOK_SECRET}")
    private String endpoindSecret;


    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeEvent(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) throws SignatureVerificationException {

        Event event = Webhook.constructEvent(payload, sigHeader, endpoindSecret);

        String checkoutCompletedEvent = "checkout.session.completed";

        if (checkoutCompletedEvent.equals(event.getType())) {
            Optional<StripeObject> stripeObject = event.getDataObjectDeserializer().getObject();

            if (stripeObject.isPresent() && stripeObject.get() instanceof Session session) {
                log.info("Extracción segura exitosa. Actualizando a Premium...");
                userService.upgradeUserToPremium(session);
                log.info("¡Usuario actualizado a Premium en la base de datos!");
            }
        }
        return ResponseEntity.ok("Recibido");
    }

}

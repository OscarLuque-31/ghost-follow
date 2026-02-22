package com.oscarluque.ghostfollowcore.web;

import com.oscarluque.ghostfollowcore.dto.subscription.PaymentRequest;
import com.oscarluque.ghostfollowcore.dto.subscription.SessionResponse;
import com.oscarluque.ghostfollowcore.service.StripeService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final StripeService stripeService;

    @PostMapping("/create-checkout-session")
    public ResponseEntity<SessionResponse> createCheckoutSession(@RequestBody PaymentRequest paymentRequest, Principal principal) throws StripeException {
        String userEmail = principal.getName();

        String urlSession = stripeService.createCheckoutSession(paymentRequest.getPlanType(), userEmail);

        return ResponseEntity.ok(new SessionResponse(urlSession));
    }

    @PostMapping("/customer-portal")
    public ResponseEntity<SessionResponse> createCustomerPortalSession(Principal principal) throws StripeException {
        String email = principal.getName();

        String portalUrl = stripeService.createCustomerPortalSession(email);

        return ResponseEntity.ok(new SessionResponse(portalUrl));
    }

}

package com.oscarluque.ghostfollowcore.service;

import com.oscarluque.ghostfollowcore.constants.PlanType;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    @Value("${STRIPE_API_KEY}")
    private String stripeApiKey;

    @Value("${STRIPE_PRICE_MONTHLY}")
    private String priceMonthly;

    @Value("${STRIPE_PRICE_LIFETIME}")
    private String priceLifetime;

    @Value("${STRIPE_SUCCESS_URL}")
    private String successUrl;

    @Value("${STRIPE_CANCEL_URL}")
    private String cancelUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    public String createCheckoutSession(String planType, String userEmail) throws StripeException {
        String priceId;
        SessionCreateParams.Mode mode;

        if (PlanType.PREMIUM_MONTHLY.name().equals(planType)) {
            priceId = priceMonthly;
            // It is save how a suscription because is monthly
            mode = SessionCreateParams.Mode.SUBSCRIPTION;
        } else if (PlanType.PREMIUM_LIFETIME.name().equals(planType)) {
            priceId = priceLifetime;
            // It is save how a payment because is unique
            mode = SessionCreateParams.Mode.PAYMENT;
        } else {
            throw new IllegalArgumentException("Tipo de plan inválido");
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(mode)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setCustomerEmail(userEmail)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPrice(priceId)
                                .build()
                ).build();

        Session session = Session.create(params);

        return session.getUrl();
    }


}

package com.oscarluque.ghostfollowcore.persistence.entity;


import com.oscarluque.ghostfollowcore.constants.PlanType;
import com.oscarluque.ghostfollowcore.constants.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    @Builder.Default
    private PlanType planType = PlanType.FREE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.NONE;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "current_period_end")
    private LocalDateTime currentPeriodEnd;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    /**
     * Determines whether the user has access to Premium features.
     */
    public boolean isActive() {
        // 1. If LIFETIME, it's always true (unless manually banned by setting status to EXPIRED)
        if (planType == PlanType.PREMIUM_LIFETIME) {
            return status != SubscriptionStatus.EXPIRED;
        }

        // 2. If MONTHLY
        // Must be ACTIVE or CANCELED (If canceled, the user remains premium until the end of the billing period)
        boolean statusOk = (status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.CANCELED);

        // And the "current period end" date must be in the future
        boolean dateOk = currentPeriodEnd == null || currentPeriodEnd.isAfter(LocalDateTime.now());

        return statusOk && dateOk;
    }
}

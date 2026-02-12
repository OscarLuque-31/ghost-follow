package com.oscarluque.ghostfollowcore.dto.subscription;

import com.oscarluque.ghostfollowcore.constants.PlanType;
import com.oscarluque.ghostfollowcore.constants.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanSubscription {
    private PlanType planType;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime currentPeriodEnd;
}

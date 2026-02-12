package com.oscarluque.ghostfollowcore.dto.response;

import com.oscarluque.ghostfollowcore.dto.subscription.PlanSubscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String instagramUserName;
    private String email;
    private PlanSubscription subscription;
}

package com.oscarluque.ghostfollowcore.dto.follower;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowerChangeEvent {
    private String accountName;
    private String eventType;
    private String targetUser;
    private String userEmail;
    private LocalDateTime localDateTime;
}

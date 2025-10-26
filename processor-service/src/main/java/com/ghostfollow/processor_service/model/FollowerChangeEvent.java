package com.ghostfollow.processor_service.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowerChangeEvent {
    private String accountId;
    private String eventType; // UNFOLLOW, FOLLOW
    private String targetUser; // Persona que creo el evento
    private LocalDateTime localDateTime;
}

package com.oscarluque.ghostfollowcore.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse {
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private String path;
}
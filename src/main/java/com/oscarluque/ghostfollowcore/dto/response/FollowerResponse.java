package com.oscarluque.ghostfollowcore.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FollowerResponse {
    private String name;
    private LocalDateTime followDate;
    private String url;
}

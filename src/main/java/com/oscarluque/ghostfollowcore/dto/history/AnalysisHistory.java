package com.oscarluque.ghostfollowcore.dto.history;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnalysisHistory {
    private LocalDateTime date;
    private Integer totalFollowers;
    private Integer gainedFollowers;
    private Integer lostFollowers;
}

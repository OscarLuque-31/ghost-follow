package com.oscarluque.ghostfollowcore.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Stats {
    private int totalFollowers;
    private int gainedCount;
    private int lostCount;
}
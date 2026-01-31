package com.oscarluque.ghostfollowcore.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AnalysisResponse {
    private Stats stats;
    private List<FollowerResponse> newFollowers;
    private List<FollowerResponse> lostFollowers;
}

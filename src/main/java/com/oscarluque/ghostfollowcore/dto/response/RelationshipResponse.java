package com.oscarluque.ghostfollowcore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelationshipResponse {
    private List<FollowerResponse> traitors;
    private List<FollowerResponse> fans;
    private List<FollowerResponse> mutuals;
}

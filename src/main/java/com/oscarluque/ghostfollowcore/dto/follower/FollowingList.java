package com.oscarluque.ghostfollowcore.dto.follower;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FollowingList {
    @JsonProperty("relationships_following")
    private List<Following> relationships;
}
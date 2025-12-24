package com.oscarluque.ghostfollowcore.dto.follower;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstagramProfile {
    @JsonProperty("href")
    private String href;
    @JsonProperty("value")
    private String value;
    @JsonProperty("timestamp")
    private long timestamp;
}

package com.oscarluque.ghostfollowcore.dto.follower;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Following {
        @JsonProperty("title")
        private String title;

        @JsonProperty("string_list_data")
        private List<InstagramProfile> stringListData;
}

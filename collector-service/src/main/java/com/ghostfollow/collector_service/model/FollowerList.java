package com.ghostfollow.collector_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FollowerList {
    private String accountId;
    private List<String> followerUsernames;
    private long timestamp;
}

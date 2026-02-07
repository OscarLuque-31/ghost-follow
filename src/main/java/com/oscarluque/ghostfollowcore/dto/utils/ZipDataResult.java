package com.oscarluque.ghostfollowcore.dto.utils;


import com.oscarluque.ghostfollowcore.dto.follower.Following;
import com.oscarluque.ghostfollowcore.dto.follower.FollowingList;
import com.oscarluque.ghostfollowcore.dto.follower.InstagramProfile;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ZipDataResult {
    @Builder.Default
    private List<InstagramProfile> followers = new ArrayList<>();

    @Builder.Default
    private List<Following> following = new ArrayList<>();
}
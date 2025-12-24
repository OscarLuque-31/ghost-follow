package com.oscarluque.ghostfollowcore.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class FollowerId {

    @Column(name = "account_id")
    private Integer accountId;

    @Column(name = "follower_username")
    private String followerUsername;
}

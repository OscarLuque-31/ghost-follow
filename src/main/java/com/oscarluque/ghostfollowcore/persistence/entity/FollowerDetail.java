package com.oscarluque.ghostfollowcore.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_followers")
@Data
public class FollowerDetail {

    @EmbeddedId
    private FollowerId id;

    @Column(name = "follower_profile_url")
    private String followerProfileUrl;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("accountId")
    @JoinColumn(name = "account_id")
    private MonitoredAccount accountState;
}
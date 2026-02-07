package com.oscarluque.ghostfollowcore.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_following")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowingDetail {

    @EmbeddedId
    private FollowingId id;

    @Column(name = "profile_url")
    private String profileUrl;

    @Column(name = "following_since")
    private LocalDateTime followingSince;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("accountId")
    @JoinColumn(name = "account_id")
    private MonitoredAccount account;
}
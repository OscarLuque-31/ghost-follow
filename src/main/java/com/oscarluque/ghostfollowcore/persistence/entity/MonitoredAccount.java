package com.oscarluque.ghostfollowcore.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
public class MonitoredAccount {

    @Id
    @Column(name = "account_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer accountId;

    @Column(name = "instagram_account_name", unique = true, nullable = false)
    private String instagramAccountName;

    @Column(name = "user_email", unique = true, nullable = false)
    private String userEmail;

    @OneToMany(mappedBy = "accountState", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FollowerDetail> followerDetails = new ArrayList<>();

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
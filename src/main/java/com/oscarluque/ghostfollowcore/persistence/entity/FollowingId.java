package com.oscarluque.ghostfollowcore.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FollowingId {

    @Column(name = "account_id")
    private Integer accountId;

    @Column(name = "username")
    private String username;
}

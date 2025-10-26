package com.ghostfollow.processor_service.entity;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class AccountFollowerState {
    @Id
    private String accountId;
    @ElementCollection
    private List<String> currentFollowers;
    private LocalDateTime lastUpdated;
}

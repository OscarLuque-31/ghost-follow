package com.oscarluque.ghostfollowcore.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id")
    private Integer accountId;

    @Column(name = "total_followers")
    private Integer totalFollowers;

    @Column(name = "gained_count")
    private Integer gainedCount;

    @Column(name = "lost_count")
    private Integer lostCount;

    @Column(name = "analysis_date")
    private LocalDateTime analysisDate;

}

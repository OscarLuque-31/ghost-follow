package com.oscarluque.ghostfollowcore.persistence.repository;

import com.oscarluque.ghostfollowcore.persistence.entity.AnalysisHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnalysisHistoryRepository extends JpaRepository<AnalysisHistory, Long> {
    List<AnalysisHistory> findByAccountIdOrderByAnalysisDateAsc(Integer accountId);
}
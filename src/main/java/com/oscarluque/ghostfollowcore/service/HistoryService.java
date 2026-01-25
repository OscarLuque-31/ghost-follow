package com.oscarluque.ghostfollowcore.service;

import com.oscarluque.ghostfollowcore.dto.history.AnalysisHistory;
import com.oscarluque.ghostfollowcore.persistence.entity.MonitoredAccount;
import com.oscarluque.ghostfollowcore.persistence.repository.AccountRepository;
import com.oscarluque.ghostfollowcore.persistence.repository.AnalysisHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final AnalysisHistoryRepository analysisHistoryRepository;
    private final AccountRepository accountRepository;


    public List<AnalysisHistory> getHistoryForEmail(String email){
        MonitoredAccount account = accountRepository.findByUserEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));

        return analysisHistoryRepository.findByAccountIdOrderByAnalysisDateAsc(account.getAccountId())
                .stream()
                .map(entity -> AnalysisHistory.builder()
                        .date(entity.getAnalysisDate())
                        .gainedFollowers(entity.getGainedCount())
                        .lostFollowers(entity.getLostCount())
                        .totalFollowers(entity.getTotalFollowers()).build())
                .collect(Collectors.toList());
    }
}

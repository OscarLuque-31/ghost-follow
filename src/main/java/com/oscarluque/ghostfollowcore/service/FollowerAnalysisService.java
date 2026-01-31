package com.oscarluque.ghostfollowcore.service;

import com.oscarluque.ghostfollowcore.dto.follower.InstagramProfile;
import com.oscarluque.ghostfollowcore.dto.response.AnalysisResponse;
import com.oscarluque.ghostfollowcore.dto.response.FollowerResponse;
import com.oscarluque.ghostfollowcore.dto.response.Stats;
import com.oscarluque.ghostfollowcore.persistence.entity.AnalysisHistory;
import com.oscarluque.ghostfollowcore.persistence.entity.FollowerDetail;
import com.oscarluque.ghostfollowcore.persistence.entity.MonitoredAccount;
import com.oscarluque.ghostfollowcore.persistence.repository.AccountRepository;
import com.oscarluque.ghostfollowcore.persistence.repository.AnalysisHistoryRepository;
import com.oscarluque.ghostfollowcore.persistence.repository.FollowerBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowerAnalysisService {

    private final AccountRepository accountRepository;
    private final FollowerBatchRepository followerBatchRepository;
    private final EmailAlertService emailAlert;
    private final AnalysisHistoryRepository analysisHistoryRepository;

    @Transactional
    public AnalysisResponse processNewFollowerList(List<InstagramProfile> currentFollowers, String accountName, String userEmail) {

        List<FollowerResponse> currentFollowersResponse = currentFollowers.stream()
                .map(instagramProfile -> FollowerResponse.builder()
                        .name(instagramProfile.getValue())
                        .followDate(LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(instagramProfile.getTimestamp()),
                                ZoneId.systemDefault()))
                        .url(instagramProfile.getHref()).build())
                .toList();

        Optional<MonitoredAccount> monitoredAccount = accountRepository.findByUserEmail(userEmail);

        List<FollowerResponse> gainedFollowers = new ArrayList<>();
        List<FollowerResponse> lostFollowers = new ArrayList<>();

        MonitoredAccount accountToSave;

        if (monitoredAccount.isPresent()) {
            accountToSave = monitoredAccount.get();

            List<FollowerDetail> oldFollowers = accountToSave.getFollowerDetails();

            Map<String, FollowerResponse> newFollowersMap = currentFollowersResponse.stream()
                    .collect(Collectors.toMap(FollowerResponse::getName, Function.identity(), (existing, replacement) -> existing));

            Set<String> oldUsernames = oldFollowers.stream()
                    .map(d -> d.getId().getFollowerUsername())
                    .collect(Collectors.toSet());

            for (FollowerDetail old : oldFollowers) {
                String username = old.getId().getFollowerUsername();
                if (!newFollowersMap.containsKey(username)) {
                    lostFollowers.add(FollowerResponse.builder()
                            .name(username)
                            .url(old.getFollowerProfileUrl())
                            .followDate(old.getLastUpdate())
                            .build());
                }
            }

            for (FollowerResponse newFollower : currentFollowersResponse) {
                if (!oldUsernames.contains(newFollower.getName())) {
                    gainedFollowers.add(newFollower);
                }
            }

            log.info("Cuenta {}: Perdidos {}, Ganados {}", accountName, lostFollowers.size(), gainedFollowers.size());

            accountToSave.setLastUpdated(LocalDateTime.now());
            accountRepository.save(accountToSave);

            followerBatchRepository.deleteAllByAccountId(accountToSave.getAccountId());
            followerBatchRepository.saveAllInBatch(currentFollowers, accountToSave.getAccountId());
        } else {
            log.info("Cuenta nueva detectada: {}. Creando registro inicial.", accountName);

            MonitoredAccount newAccount = new MonitoredAccount();
            newAccount.setInstagramAccountName(accountName);
            newAccount.setUserEmail(userEmail);
            newAccount.setLastUpdated(LocalDateTime.now());

            accountToSave = accountRepository.save(newAccount);
            followerBatchRepository.saveAllInBatch(currentFollowers, accountToSave.getAccountId());
        }

        if (!lostFollowers.isEmpty() || !gainedFollowers.isEmpty()) {
            List<String> lostNames = lostFollowers.stream().map(FollowerResponse::getName).toList();
            List<String> gainedNames = gainedFollowers.stream().map(FollowerResponse::getName).toList();
            emailAlert.sendSummaryEmail(userEmail, accountName, lostNames, gainedNames);
        }

        AnalysisHistory history = AnalysisHistory.builder()
                .accountId(accountToSave.getAccountId())
                .totalFollowers(currentFollowers.size())
                .gainedCount(gainedFollowers.size())
                .lostCount(lostFollowers.size())
                .analysisDate(LocalDateTime.now())
                .build();

        analysisHistoryRepository.save(history);

        return AnalysisResponse.builder()
                .stats(Stats.builder()
                        .totalFollowers(currentFollowersResponse.size())
                        .gainedCount(gainedFollowers.size())
                        .lostCount(lostFollowers.size())
                        .build())
                .newFollowers(gainedFollowers)
                .lostFollowers(lostFollowers)
                .build();
    }
}
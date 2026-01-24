package com.oscarluque.ghostfollowcore.service;

import com.oscarluque.ghostfollowcore.dto.follower.InstagramProfile;
import com.oscarluque.ghostfollowcore.dto.response.AnalysisResponse;
import com.oscarluque.ghostfollowcore.dto.response.Stats;
import com.oscarluque.ghostfollowcore.persistence.entity.FollowerDetail;
import com.oscarluque.ghostfollowcore.persistence.entity.MonitoredAccount;
import com.oscarluque.ghostfollowcore.persistence.repository.AccountRepository;
import com.oscarluque.ghostfollowcore.persistence.repository.FollowerBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowerAnalysisService {

    private final AccountRepository accountRepository;
    private final FollowerBatchRepository followerBatchRepository;
    private final EmailAlertService emailAlert;

    @Transactional
    public AnalysisResponse processNewFollowerList(List<InstagramProfile> currentFollowers, String accountName, String userEmail) {

        // Step 1: Extraer usernames nuevos
        List<String> newUsernames = currentFollowers.stream()
                .map(InstagramProfile::getValue)
                .toList();

        // Step 2: Buscar cuenta existente
        Optional<MonitoredAccount> accountOpt = accountRepository.findByUserEmail(userEmail);

        List<String> gainedFollowers = new ArrayList<>();
        List<String> lostFollowers = new ArrayList<>();
        MonitoredAccount accountToSave;

        if (accountOpt.isPresent()) {
            accountToSave = accountOpt.get();

            List<String> oldUsernames = extractUsernames(accountToSave.getFollowerDetails());

            Set<String> oldSet = new HashSet<>(oldUsernames);
            Set<String> newSet = new HashSet<>(newUsernames);

            for (String user : oldSet) {
                if (!newSet.contains(user)) lostFollowers.add(user);
            }

            for (String user : newSet) {
                if (!oldSet.contains(user)) gainedFollowers.add(user);
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
            emailAlert.sendSummaryEmail(userEmail, accountName, lostFollowers, gainedFollowers);
        }

        return AnalysisResponse.builder()
                .stats(Stats.builder()
                        .totalFollowers(newUsernames.size())
                        .gainedCount(gainedFollowers.size())
                        .lostCount(lostFollowers.size())
                        .build())
                .newFollowers(gainedFollowers)
                .lostFollowers(lostFollowers)
                .build();
    }

    private List<String> extractUsernames(List<FollowerDetail> followerDetails) {
        if (followerDetails == null) return List.of();
        return followerDetails.stream()
                .map(detail -> detail.getId().getFollowerUsername())
                .toList();
    }
}
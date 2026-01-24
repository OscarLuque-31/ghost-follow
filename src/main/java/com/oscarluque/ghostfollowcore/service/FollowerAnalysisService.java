package com.oscarluque.ghostfollowcore.service;

import com.oscarluque.ghostfollowcore.dto.follower.InstagramProfile;
import com.oscarluque.ghostfollowcore.dto.response.AnalysisResponse;
import com.oscarluque.ghostfollowcore.dto.response.Stats;
import com.oscarluque.ghostfollowcore.persistence.entity.FollowerDetail;
import com.oscarluque.ghostfollowcore.persistence.entity.FollowerId;
import com.oscarluque.ghostfollowcore.persistence.entity.MonitoredAccount;
import com.oscarluque.ghostfollowcore.persistence.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FollowerAnalysisService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FollowerAnalysisService.class);

    @Autowired
    private AccountRepository repository;

    @Autowired
    private EmailAlertService emailAlert;

    @Transactional
    public AnalysisResponse processNewFollowerList(List<InstagramProfile> currentFollowers, String accountName, String userEmail) {

        // Step 1: Prepare the new list of usernames
        List<String> newUsernames = currentFollowers.stream()
                .map(InstagramProfile::getValue)
                .toList();

        // Step 2: Search the old state in bbdd
        Optional<MonitoredAccount> accountOpt = repository.findByUserEmail(userEmail);

        List<String> oldUsernames;
        List<String> gainedFollowers = new ArrayList<>();
        List<String> lostFollowers = new ArrayList<>();

        MonitoredAccount accountToSave;

        if (accountOpt.isPresent()) {
            accountToSave = accountOpt.get();
            oldUsernames = extractUsernames(accountToSave.getFollowerDetails());

            Set<String> oldSet = new HashSet<>(oldUsernames);
            Set<String> newSet = new HashSet<>(newUsernames);

            // UNFOLLOWERS
            for (String user : oldSet) {
                if (!newSet.contains(user)) {
                    lostFollowers.add(user);
                }
            }

            // NEW FOLLOWERS
            for (String user : newSet) {
                if (!oldSet.contains(user)) {
                    gainedFollowers.add(user);
                }
            }

            LOGGER.info("Cuenta {}: Perdidos {}, Ganados {}", accountName, lostFollowers.size(), gainedFollowers.size());

            List<FollowerDetail> newDetailsEntities = convertToFollowersDetails(currentFollowers, accountToSave.getAccountId(), accountToSave);

            accountToSave.getFollowerDetails().clear();
            accountToSave.getFollowerDetails().addAll(newDetailsEntities);
            accountToSave.setLastUpdated(LocalDateTime.now());
        } else {
            LOGGER.info("Cuenta nueva detectada: {}. Creando registro inicial.", accountName);

            MonitoredAccount newAccount = new MonitoredAccount();
            newAccount.setInstagramAccountName(accountName);
            newAccount.setUserEmail(userEmail);

            // Guardamos primero para generar el ID
            accountToSave = repository.save(newAccount);

            List<FollowerDetail> initialDetails = convertToFollowersDetails(currentFollowers, accountToSave.getAccountId(), accountToSave);
            newAccount.setFollowerDetails(initialDetails);
            newAccount.setLastUpdated(LocalDateTime.now());
        }

        repository.save(accountToSave);

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
                .collect(Collectors.toList());
    }

    private List<FollowerDetail> convertToFollowersDetails(List<InstagramProfile> entries, Integer accountPkId, MonitoredAccount state) {
        return entries.stream()
                .map(entry -> {
                    FollowerDetail detail = new FollowerDetail();
                    FollowerId followerId = new FollowerId();
                    followerId.setAccountId(accountPkId);
                    followerId.setFollowerUsername(entry.getValue());
                    detail.setId(followerId);

                    detail.setFollowerProfileUrl(entry.getHref());
                    detail.setLastUpdate(LocalDateTime.ofEpochSecond(entry.getTimestamp(), 0, java.time.ZoneOffset.UTC));
                    detail.setAccountState(state);
                    return detail;
                })
                .collect(Collectors.toList());
    }
}

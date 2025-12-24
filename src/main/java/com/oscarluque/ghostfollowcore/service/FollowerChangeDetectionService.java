package com.oscarluque.ghostfollowcore.service;

import com.oscarluque.ghostfollowcore.constants.EventType;
import com.oscarluque.ghostfollowcore.persistence.entity.MonitoredAccount;
import com.oscarluque.ghostfollowcore.persistence.entity.FollowerDetail;
import com.oscarluque.ghostfollowcore.persistence.entity.FollowerId;
import com.oscarluque.ghostfollowcore.persistence.repository.AccountRepository;
import com.oscarluque.ghostfollowcore.dto.follower.FollowerChangeEvent;
import com.oscarluque.ghostfollowcore.dto.follower.InstagramProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FollowerChangeDetectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FollowerChangeDetectionService.class);

    @Autowired
    private AccountRepository repository;

    @Autowired
    private EmailAlertService emailAlert;

    /**
     * @Async: Ejecuta este método en un hilo separado.
     * El Controller responde "202 Accepted" y esto sigue trabajando.
     */
    @Async("taskExecutor")
    @Transactional
    public void processNewFollowerList(List<InstagramProfile> currentFollowers, String accountName, String userEmail) {

        List<String> newFollowerUsernames = currentFollowers.stream()
                .map(InstagramProfile::getValue)
                .collect(Collectors.toList());

        repository.findByUserEmail(userEmail).ifPresentOrElse(
                (previousState) -> {
                    List<String> oldFollowerUsernames = extractUsernames(previousState.getFollowerDetails());

                    detectAndPublishChanges(oldFollowerUsernames, newFollowerUsernames, accountName, userEmail);

                    List<FollowerDetail> newDetails = convertToFollowersDetails(currentFollowers, previousState.getAccountId(), previousState);

                    previousState.getFollowerDetails().clear();
                    previousState.getFollowerDetails().addAll(newDetails);
                    previousState.setLastUpdated(LocalDateTime.now());
                    repository.save(previousState);

                    LOGGER.info("Estado de la cuenta {} actualizado.", accountName);
                },
                () -> {
                    MonitoredAccount newState = new MonitoredAccount();
                    newState.setInstagramAccountName(accountName);
                    newState.setUserEmail(userEmail);

                    MonitoredAccount savedState = repository.save(newState);
                    Integer generatedAccountId = savedState.getAccountId();

                    List<FollowerDetail> newDetails = convertToFollowersDetails(currentFollowers, generatedAccountId, newState);

                    newState.setFollowerDetails(newDetails);
                    newState.setLastUpdated(LocalDateTime.now());

                    repository.save(newState);
                    LOGGER.info("Estado inicial de la cuenta {} guardado.", accountName);
                }
        );
    }

    private void detectAndPublishChanges(List<String> oldFollowers, List<String> newFollowers, String accountName, String userEmail){
        Set<String> oldSetFollowers = new HashSet<>(oldFollowers);
        Set<String> newSetFollowers = new HashSet<>(newFollowers);

        Set<String> unfollowed = new HashSet<>(oldSetFollowers);
        unfollowed.removeAll(newSetFollowers);

        unfollowed.forEach(user -> {
            FollowerChangeEvent event = createEvent(accountName, userEmail , EventType.UNFOLLOWED.name(), user);
            emailAlert.sendUnFollowAlert(event);
            LOGGER.info("UNFOLLOW detectado: {}. Alerta enviada.", user);
        });
    }

    private FollowerChangeEvent createEvent(String accountName, String userEmail, String eventType, String targetUser) {
        FollowerChangeEvent followerChangeEvent = new FollowerChangeEvent();
        followerChangeEvent.setAccountName(accountName);
        followerChangeEvent.setUserEmail(userEmail);
        followerChangeEvent.setEventType(eventType);
        followerChangeEvent.setTargetUser(targetUser);
        followerChangeEvent.setLocalDateTime(LocalDateTime.now());
        return followerChangeEvent;
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


package com.ghostfollow.processor_service.listener;

import com.fasterxml.jackson.datatype.jdk8.OptionalLongDeserializer;
import com.ghostfollow.collector_service.model.FollowerList;
import com.ghostfollow.processor_service.entity.AccountFollowerState;
import com.ghostfollow.processor_service.model.FollowerChangeEvent;
import com.ghostfollow.processor_service.repository.FollowerStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RawDataConsumer {

    @Autowired
    private FollowerStateRepository repository;

    @Autowired
    private KafkaTemplate<String, FollowerChangeEvent> kafkaTemplate;

    private static final String EVENTS_TOPIC = "follower-change-events";
    private static final String RAW_TOPIC = "raw-follower-lists";
    private static final String GROUP_ID = "processor-group";

    @KafkaListener(topics = RAW_TOPIC, groupId = GROUP_ID, containerFactory = "kafkaListenerContainerFactory")
    public void consumerRawData(FollowerList newFollowerList) {
        String accountId = newFollowerList.getAccountId();
        List<String> currentFollowers = newFollowerList.getFollowerUsernames();

        repository.findById(accountId).ifPresentOrElse(
                (previousState) -> {

                    detectAndPublishChanges(previousState.getCurrentFollowers(), currentFollowers, accountId);
                    previousState.setCurrentFollowers(currentFollowers);
                    previousState.setLastUpdated(LocalDateTime.now());
                    repository.save(previousState);

                    System.out.println("Estado de la cuenta " + accountId + " actualizado.");
                },
                () -> {
                    AccountFollowerState newState = new AccountFollowerState();
                    newState.setAccountId(accountId);
                    newState.setCurrentFollowers(currentFollowers);
                    newState.setLastUpdated(LocalDateTime.now());

                    repository.save(newState);
                    System.out.println("Estado inicial de la cuenta " + accountId + " guardado.");
                }
        );
    }

    private void detectAndPublishChanges(List<String> oldFollowers, List<String> newFollowers, String accountId){

        Set<String> oldSetFollowers = new HashSet<>(oldFollowers);
        Set<String> newSetFollowers = new HashSet<>(newFollowers);

        // Detección de UNFOLLOWS
        Set<String> unfollowed = new HashSet<>(oldSetFollowers);
        unfollowed.removeAll(newSetFollowers);

        // Detección de FOLLOWS
        Set<String> newFollows = new HashSet<>(newSetFollowers);
        newFollows.removeAll(oldSetFollowers);

        // Publicar eventos de UNFOLLOW
        unfollowed.forEach(user -> {
            FollowerChangeEvent event = createEvent(accountId, "UNFOLLOWED", user);
            kafkaTemplate.send(EVENTS_TOPIC, event.getAccountId(), event);
            System.out.println("UNFOLLOW de " + user);
        });

        // Publicar eventos de FOLLOW
        newFollows.forEach(user -> {
            FollowerChangeEvent event = createEvent(accountId, "NEWFOLLOW", user);
            kafkaTemplate.send(EVENTS_TOPIC, event.getAccountId(), event);
            System.out.println("NEWFOLLOW de " + user);
        });
    }

    private FollowerChangeEvent createEvent(String accountId, String eventType, String targetUser) {
        FollowerChangeEvent followerChangeEvent = new FollowerChangeEvent();
        followerChangeEvent.setAccountId(accountId);
        followerChangeEvent.setEventType(eventType);
        followerChangeEvent.setTargetUser(targetUser);
        followerChangeEvent.setLocalDateTime(LocalDateTime.now());
        return followerChangeEvent;
    }

}

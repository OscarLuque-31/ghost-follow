package com.oscarluque.ghostfollowcore.service;

import com.oscarluque.ghostfollowcore.dto.response.FollowerResponse;
import com.oscarluque.ghostfollowcore.dto.response.RelationshipResponse;
import com.oscarluque.ghostfollowcore.persistence.entity.FollowerDetail;
import com.oscarluque.ghostfollowcore.persistence.entity.FollowingDetail;
import com.oscarluque.ghostfollowcore.persistence.entity.MonitoredAccount;
import com.oscarluque.ghostfollowcore.persistence.repository.AccountFollowersRepository;
import com.oscarluque.ghostfollowcore.persistence.repository.AccountFollowingRepository;
import com.oscarluque.ghostfollowcore.persistence.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelationshipService {

    private final AccountRepository accountRepository;
    private final AccountFollowingRepository accountFollowingRepository;
    private final AccountFollowersRepository accountFollowersRepository;

    public RelationshipResponse processRelationshipLists(String userEmail) {

        Integer accountId = getAccountIdByEmail(userEmail);

        Map<String, String> followersMap = getFollowersMap(accountId);
        Map<String, String> followingMap = getFollowingMap(accountId);

        return RelationshipResponse.builder()
                // Traitors: Users I follow, but they do NOT follow me back (Present in Following, missing in Followers)
                .traitors(filterByRelationship(followingMap, name -> !followersMap.containsKey(name)))

                // Fans: Users who follow me, but I do NOT follow them back (Present in Followers, missing in Following)
                .fans(filterByRelationship(followersMap, name -> !followingMap.containsKey(name)))

                // Mutuals: Users I follow AND who follow me back (Present in both lists)
                .mutuals(filterByRelationship(followingMap, name -> followersMap.containsKey(name)))
                .build();
    }

    /**
     * Generic method to filter users based on a condition and map them directly to DTOs.
     * * @param sourceMap The source map containing the data (Username -> Profile URL).
     * @param condition The predicate condition to determine if a user should be included.
     * @return A list of FollowerResponse objects.
     */
    private List<FollowerResponse> filterByRelationship(Map<String, String> sourceMap, Predicate<String> condition) {
        return sourceMap.entrySet().stream()
                .filter(entry -> condition.test(entry.getKey()))
                .map(entry -> FollowerResponse.builder()
                        .name(entry.getKey())
                        .url(entry.getValue())
                        .build())
                .toList();
    }

    private Integer getAccountIdByEmail(String email) {
        return accountRepository.findByUserEmail(email)
                .map(MonitoredAccount::getAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + email));
    }

    private Map<String, String> getFollowersMap(Integer accountId) {
        return accountFollowersRepository.findById_AccountId(accountId).stream()
                .collect(Collectors.toMap(
                        followerDetail -> followerDetail.getId().getFollowerUsername(),
                        FollowerDetail::getFollowerProfileUrl,
                        (existing, replacement) -> existing
                ));
    }

    private Map<String, String> getFollowingMap(Integer accountId) {
        return accountFollowingRepository.findById_AccountId(accountId).stream()
                .collect(Collectors.toMap(
                        followingDetail -> followingDetail.getId().getUsername(),
                        FollowingDetail::getProfileUrl,
                        (existing, replacement) -> existing
                ));
    }

}
package com.oscarluque.ghostfollowcore.service;

import com.oscarluque.ghostfollowcore.dto.follower.Following;
import com.oscarluque.ghostfollowcore.dto.follower.InstagramProfile;
import com.oscarluque.ghostfollowcore.persistence.entity.FollowingDetail;
import com.oscarluque.ghostfollowcore.persistence.entity.FollowingId;
import com.oscarluque.ghostfollowcore.persistence.entity.MonitoredAccount;
import com.oscarluque.ghostfollowcore.persistence.repository.AccountFollowingRepository;
import com.oscarluque.ghostfollowcore.persistence.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowingService {

    private final AccountRepository accountRepository;
    private final AccountFollowingRepository accountFollowingRepository;

    @Transactional
    public void processFollowingList(List<Following> currentFollowingList, String userEmail) {

        MonitoredAccount monitoredAccount = accountRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada: " + userEmail));

        log.info("Cuenta encontrada: {}", monitoredAccount);

        accountFollowingRepository.deleteByAccountId(monitoredAccount.getAccountId());

        log.info("Followings de la cuenta {} borrados", monitoredAccount);

        List<FollowingDetail> followingDetails = mapFollowings(currentFollowingList, monitoredAccount);

        log.info("Mapeos a Following Details");

        accountFollowingRepository.saveAll(followingDetails);
        log.info("✅ Guardados {} seguidos para la cuenta {}", followingDetails.size(), monitoredAccount.getInstagramAccountName());
    }

    private List<FollowingDetail> mapFollowings(List<Following> currentFollowingList, MonitoredAccount account) {

        return currentFollowingList.stream().map(following -> {

                    if (following.getStringListData() == null || following.getStringListData().isEmpty()) {
                        return null;
                    }

                    InstagramProfile profile = following.getStringListData().get(0);

                    return FollowingDetail.builder()
                            .id(new FollowingId(account.getAccountId(), following.getTitle()))
                            .followingSince(
                                    LocalDateTime.ofInstant(
                                            Instant.ofEpochSecond(profile.getTimestamp()),
                                            ZoneId.systemDefault()))
                            .account(account)
                            .profileUrl(profile.getHref())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }
}

package com.oscarluque.ghostfollowcore.service;

import com.oscarluque.ghostfollowcore.dto.response.FollowerResponse;
import com.oscarluque.ghostfollowcore.persistence.entity.FollowerDetail;
import com.oscarluque.ghostfollowcore.persistence.entity.MonitoredAccount;
import com.oscarluque.ghostfollowcore.persistence.repository.AccountFollowersRepository;
import com.oscarluque.ghostfollowcore.persistence.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowersService {

    private final AccountRepository accountRepository;
    private final AccountFollowersRepository accountFollowersRepository;

    public List<FollowerResponse> getFollowersByEmail(String email) {

        MonitoredAccount monitoredAccount = accountRepository.findByUserEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada: " + email));

        List<FollowerDetail> followerDetails = accountFollowersRepository.findByAccountId(monitoredAccount.getAccountId()).orElse(List.of());

        return followerDetails.stream()
                .map(entity -> FollowerResponse.builder()
                        .name(entity.getAccountState().getInstagramAccountName())
                        .followDate(entity.getLastUpdate())
                        .url(entity.getFollowerProfileUrl())
                        .build())
                .toList();
    }
}

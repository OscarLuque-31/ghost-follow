package com.oscarluque.ghostfollowcore.service;

import com.oscarluque.ghostfollowcore.config.security.JwtService;
import com.oscarluque.ghostfollowcore.constants.PlanType;
import com.oscarluque.ghostfollowcore.constants.SubscriptionStatus;
import com.oscarluque.ghostfollowcore.dto.auth.AuthRequest;
import com.oscarluque.ghostfollowcore.dto.auth.AuthResponse;
import com.oscarluque.ghostfollowcore.persistence.entity.MonitoredAccount;
import com.oscarluque.ghostfollowcore.persistence.entity.Subscription;
import com.oscarluque.ghostfollowcore.persistence.entity.User;
import com.oscarluque.ghostfollowcore.persistence.repository.AccountRepository;
import com.oscarluque.ghostfollowcore.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(AuthRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya existe");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Subscription freeSubscription = Subscription.builder()
                .user(user)
                .planType(PlanType.FREE)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDateTime.now())
                .build();

        user.setSubscription(freeSubscription);

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }

    public String getCurrentUserInstagramName() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        MonitoredAccount monitoredAccount = accountRepository.findByUserEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No hay cuenta de Instagram vinculada"));

        return monitoredAccount.getInstagramAccountName();
    }
}
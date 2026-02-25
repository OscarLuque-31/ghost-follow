package com.oscarluque.ghostfollowcore.service;

import com.oscarluque.ghostfollowcore.config.security.JwtService;
import com.oscarluque.ghostfollowcore.constants.PlanType;
import com.oscarluque.ghostfollowcore.constants.SubscriptionStatus;
import com.oscarluque.ghostfollowcore.dto.auth.AuthRequest;
import com.oscarluque.ghostfollowcore.dto.auth.AuthResponse;
import com.oscarluque.ghostfollowcore.dto.auth.ResetPasswordRequest;
import com.oscarluque.ghostfollowcore.dto.response.UserResponse;
import com.oscarluque.ghostfollowcore.dto.subscription.PlanSubscription;
import com.oscarluque.ghostfollowcore.persistence.entity.MonitoredAccount;
import com.oscarluque.ghostfollowcore.persistence.entity.PasswordResetCode;
import com.oscarluque.ghostfollowcore.persistence.entity.Subscription;
import com.oscarluque.ghostfollowcore.persistence.entity.User;
import com.oscarluque.ghostfollowcore.persistence.repository.AccountRepository;
import com.oscarluque.ghostfollowcore.persistence.repository.PasswordResetCodeRepository;
import com.oscarluque.ghostfollowcore.persistence.repository.SubscriptionRepository;
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
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordResetCodeRepository codeRepository;
    private final EmailAlertService emailAlertService;

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

        MonitoredAccount monitoredAccount = new MonitoredAccount();
        monitoredAccount.setInstagramAccountName(request.getInstagramUsername());
        monitoredAccount.setUserEmail(request.getEmail());
        monitoredAccount.setLastUpdated(LocalDateTime.now());

        accountRepository.save(monitoredAccount);

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

    public UserResponse getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        MonitoredAccount monitoredAccount = accountRepository.findByUserEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No hay cuenta de Instagram vinculada"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No se ha encontrado usuario"));

        Subscription subscription = subscriptionRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("No existe subscripcion para este usuario"));

        boolean hasData = monitoredAccount != null
                && monitoredAccount.getFollowerDetails() != null
                && !monitoredAccount.getFollowerDetails().isEmpty();

        PlanSubscription planSubscription = PlanSubscription.builder()
                .planType(subscription.getPlanType())
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                .build();


        return UserResponse.builder()
                .instagramUserName(monitoredAccount.getInstagramAccountName())
                .email(email)
                .hasInitialData(hasData)
                .subscription(planSubscription)
                .build();
    }

    @Transactional
    public void forgotPassword(String email) {
        String code = String.format("%06d", new Random().nextInt(999999));

        codeRepository.deleteByEmail(email);

        PasswordResetCode resetCode = PasswordResetCode.builder()
                .code(code)
                .email(email)
                .expirationTime(LocalDateTime.now().plusMinutes(15))
                .build();

        codeRepository.save(resetCode);

        emailAlertService.sendPasswordResetCode(email, code);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetCode resetCode = codeRepository.findByEmailAndCode(request.getEmail(), request.getCode())
                .orElseThrow(() -> new IllegalArgumentException("El código es incorrecto o no existe"));

        if (resetCode.getExpirationTime().isBefore(LocalDateTime.now())) {
            codeRepository.delete(resetCode);
            throw new IllegalArgumentException("El código ha caducado. Por favor, solicita uno nuevo.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        codeRepository.delete(resetCode);
    }

    @Transactional
    public void verifyCode(String email, String code) {
        PasswordResetCode resetCode = codeRepository.findByEmailAndCode(email, code)
                .orElseThrow(() -> new IllegalArgumentException("El código es incorrecto."));

        if (resetCode.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El código ha caducado.");
        }
    }
}
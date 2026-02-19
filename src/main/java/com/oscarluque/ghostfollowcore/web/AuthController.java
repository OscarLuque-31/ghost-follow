package com.oscarluque.ghostfollowcore.web;

import com.oscarluque.ghostfollowcore.dto.auth.*;
import com.oscarluque.ghostfollowcore.dto.response.MessageResponse;
import com.oscarluque.ghostfollowcore.dto.response.UserResponse;
import com.oscarluque.ghostfollowcore.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());

        return ResponseEntity.ok(new MessageResponse("Si el correo está registrado, recibirás un código en breve."));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<MessageResponse> verifyCode(@RequestBody VerifyCodeRequest request) {
        authService.verifyCode(request.getEmail(), request.getCode());

        return ResponseEntity.ok(new MessageResponse("Código válido"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);

        return ResponseEntity.ok(new MessageResponse("Contraseña actualizada exitosamente."));
    }



}


package com.oscarluque.ghostfollowcore.dto.auth;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String email;
    private String code;
    private String newPassword;
}
package com.oscarluque.ghostfollowcore.dto.auth;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String email;
    private String newPassword;
}

package com.oscarluque.ghostfollowcore.dto.auth;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
    private String instagramUsername;
}

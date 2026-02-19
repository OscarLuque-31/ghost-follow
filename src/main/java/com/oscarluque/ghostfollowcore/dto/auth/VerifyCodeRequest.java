package com.oscarluque.ghostfollowcore.dto.auth;

import lombok.Data;

@Data
public class VerifyCodeRequest {
    private String email;
    private String code;
}
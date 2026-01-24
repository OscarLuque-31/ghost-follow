package com.oscarluque.ghostfollowcore.web;

import com.oscarluque.ghostfollowcore.dto.follower.InstagramProfile;
import com.oscarluque.ghostfollowcore.dto.follower.FollowerWrapper;
import com.oscarluque.ghostfollowcore.dto.response.AnalysisResponse;
import com.oscarluque.ghostfollowcore.service.FollowerChangeDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/followers")
public class FollowerUploadController {

    @Autowired
    private FollowerChangeDetectionService detectionService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadList(@RequestBody MultipartFile file, @RequestParam String accountName) {
        try {
            String authenticatedEmail = SecurityContextHolder.getContext().getAuthentication().getName();

            AnalysisResponse analysisResponse = detectionService.processFollowerFile(file, accountName, authenticatedEmail);

            return ResponseEntity.ok(analysisResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error interno procesando el archivo.");
        }
    }

}

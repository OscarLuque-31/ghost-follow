package com.oscarluque.ghostfollowcore.web;

import com.oscarluque.ghostfollowcore.dto.response.AnalysisResponse;
import com.oscarluque.ghostfollowcore.service.FollowerChangeDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/followers")
@RequiredArgsConstructor
public class FollowerUploadController {

    private final FollowerChangeDetectionService detectionService;

    @PostMapping("/upload")
    public ResponseEntity<AnalysisResponse> uploadList(@RequestBody MultipartFile file, @RequestParam String accountName) throws IOException {
        String authenticatedEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        AnalysisResponse analysisResponse = detectionService.processFollowerFile(file, accountName, authenticatedEmail);

        return ResponseEntity.ok(analysisResponse);
    }
}

package com.oscarluque.ghostfollowcore.web;

import com.oscarluque.ghostfollowcore.dto.response.AnalysisResponse;
import com.oscarluque.ghostfollowcore.dto.response.FollowerResponse;
import com.oscarluque.ghostfollowcore.service.FollowerChangeDetectionService;
import com.oscarluque.ghostfollowcore.service.FollowersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/followers")
@RequiredArgsConstructor
public class FollowerController {

    private final FollowerChangeDetectionService detectionService;
    private final FollowersService followersService;

    @PostMapping("/upload")
    public ResponseEntity<AnalysisResponse> uploadList(@RequestBody MultipartFile file, @RequestParam String accountName) throws IOException {
        String authenticatedEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        AnalysisResponse analysisResponse = detectionService.processFollowerFile(file, accountName, authenticatedEmail);

        return ResponseEntity.ok(analysisResponse);
    }


    @GetMapping
    public ResponseEntity<List<FollowerResponse>> getFollowersByEmail() throws IOException {
        String authenticatedEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        List<FollowerResponse> followerResponse = followersService.getFollowersByEmail(authenticatedEmail);

        return ResponseEntity.ok(followerResponse);
    }
}

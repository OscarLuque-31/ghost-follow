package com.oscarluque.ghostfollowcore.web;

import com.oscarluque.ghostfollowcore.dto.follower.InstagramProfile;
import com.oscarluque.ghostfollowcore.dto.follower.FollowerWrapper;
import com.oscarluque.ghostfollowcore.dto.response.AnalysisResponse;
import com.oscarluque.ghostfollowcore.service.FollowerChangeDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/followers")
public class FollowerUploadController {

    @Autowired
    private FollowerChangeDetectionService detectionService;

    @PostMapping("/upload")
    public ResponseEntity<AnalysisResponse> uploadList(@RequestBody List<FollowerWrapper> containerList, @RequestParam String accountName) {

        List<InstagramProfile> allFollowers = new ArrayList<>();

        if (containerList != null) {
            for (FollowerWrapper container : containerList) {
                if (container.getFollowerEntryList() != null) {
                    allFollowers.addAll(container.getFollowerEntryList());
                }
            }
        }

        if (allFollowers.isEmpty()) {
            return ResponseEntity.badRequest().body(AnalysisResponse.builder().build());
        }

        String authenticatedEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        AnalysisResponse analysisResponse = detectionService.processNewFollowerList(allFollowers, accountName, authenticatedEmail);

        return ResponseEntity.accepted().body(analysisResponse);
    }

}

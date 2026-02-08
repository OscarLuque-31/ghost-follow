package com.oscarluque.ghostfollowcore.web;

import com.oscarluque.ghostfollowcore.dto.response.RelationshipResponse;
import com.oscarluque.ghostfollowcore.service.RelationshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/relationship")
public class RelationshipController {

    private final RelationshipService relationshipService;

    @GetMapping
    public ResponseEntity<RelationshipResponse> getRelationship() {
        String authenticatedEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        RelationshipResponse relationshipResponse = relationshipService.processRelationshipLists(authenticatedEmail);

        return ResponseEntity.ok(relationshipResponse);
    }
}

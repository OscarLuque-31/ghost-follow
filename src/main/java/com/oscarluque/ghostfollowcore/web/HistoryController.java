package com.oscarluque.ghostfollowcore.web;

import com.oscarluque.ghostfollowcore.dto.history.AnalysisHistory;
import com.oscarluque.ghostfollowcore.service.HistoryService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public ResponseEntity<List<AnalysisHistory>> getAccountHistory() {
        String authenticatedEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        return ResponseEntity.ok(historyService.getHistoryForEmail(authenticatedEmail));
    }

}

package com.retrievo.controller;

import com.retrievo.dto.DashboardSummaryDTO;
import com.retrievo.entity.User;
import com.retrievo.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dashboardService.getSummary(user));
    }
}

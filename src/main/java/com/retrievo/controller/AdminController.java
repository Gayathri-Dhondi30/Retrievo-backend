package com.retrievo.controller;

import com.retrievo.dto.ItemDTO;
import com.retrievo.dto.UserDTO;
import com.retrievo.entity.AdminAuditLog;
import com.retrievo.entity.User;
import com.retrievo.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        return ResponseEntity.ok(adminService.getAdminDashboardStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<UserDTO> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User adminUser
    ) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, body.get("status"), adminUser));
    }

    @GetMapping("/reports")
    public ResponseEntity<List<ItemDTO>> getAllReports() {
        return ResponseEntity.ok(adminService.getAllReportsForModeration());
    }

    @DeleteMapping("/reports/{id}")
    public ResponseEntity<Map<String, String>> deleteReport(
            @PathVariable Long id,
            @AuthenticationPrincipal User adminUser
    ) {
        adminService.deleteReportAdmin(id, adminUser);
        return ResponseEntity.ok(Map.of("message", "Report deleted by admin."));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AdminAuditLog>> getAuditLogs() {
        return ResponseEntity.ok(adminService.getAuditLogs());
    }
}

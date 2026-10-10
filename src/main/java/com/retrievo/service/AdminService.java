package com.retrievo.service;

import com.retrievo.dto.ItemDTO;
import com.retrievo.dto.UserDTO;
import com.retrievo.entity.*;
import com.retrievo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private AdminAuditLogRepository auditLogRepository;

    public Map<String, Object> getAdminDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userRepository.count();
        long totalReports = itemRepository.count();
        long recoveredCount = itemRepository.countByStatus(ItemStatus.RECOVERED);

        double recoveryRate = totalReports > 0 ? ((double) recoveredCount / totalReports) * 100.0 : 0.0;

        stats.put("totalUsers", totalUsers);
        stats.put("totalReports", totalReports);
        stats.put("recoveredCount", recoveredCount);
        stats.put("recoveryRate", Math.round(recoveryRate * 10.0) / 10.0);

        // Reports grouped by category
        Map<String, Long> categoryMap = new HashMap<>();
        List<Item> allItems = itemRepository.findAll();
        allItems.forEach(i -> categoryMap.put(i.getCategory(), categoryMap.getOrDefault(i.getCategory(), 0L) + 1));
        stats.put("reportsByCategory", categoryMap);

        return stats;
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(UserDTO::new).toList();
    }

    @Transactional
    public UserDTO updateUserStatus(Long userId, String status, User adminUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        user.setStatus(status);
        User saved = userRepository.save(user);

        logAudit(adminUser, "UPDATE_USER_STATUS", "USER", String.valueOf(userId), "Updated status to " + status);
        return new UserDTO(saved);
    }

    public List<ItemDTO> getAllReportsForModeration() {
        return itemRepository.findAll().stream().map(ItemDTO::new).toList();
    }

    @Transactional
    public void deleteReportAdmin(Long reportId, User adminUser) {
        Item item = itemRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found."));

        itemRepository.delete(item);
        logAudit(adminUser, "DELETE_REPORT", "ITEM", String.valueOf(reportId), "Deleted item report: " + item.getTitle());
    }

    public List<AdminAuditLog> getAuditLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }

    private void logAudit(User admin, String action, String targetType, String targetId, String details) {
        AdminAuditLog log = new AdminAuditLog(admin, action, targetType, targetId, details, "127.0.0.1");
        auditLogRepository.save(log);
    }
}

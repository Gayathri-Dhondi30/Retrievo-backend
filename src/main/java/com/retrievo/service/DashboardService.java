package com.retrievo.service;

import com.retrievo.dto.DashboardSummaryDTO;
import com.retrievo.entity.ClaimStatus;
import com.retrievo.entity.ItemStatus;
import com.retrievo.entity.ItemType;
import com.retrievo.entity.User;
import com.retrievo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemMatchRepository itemMatchRepository;

    @Autowired
    private ClaimRepository claimRepository;

    public DashboardSummaryDTO getSummary(User currentUser) {
        DashboardSummaryDTO summary = new DashboardSummaryDTO();

        // System global stats
        summary.setTotalReports(itemRepository.count());
        summary.setFoundCount(itemRepository.countByItemType(ItemType.FOUND));
        summary.setRecoveredCount(itemRepository.countByStatus(ItemStatus.RECOVERED));
        summary.setActiveUsers(userRepository.countByStatus("ACTIVE"));

        // User personal stats if logged in
        if (currentUser != null) {
            summary.setUserTotalReports(itemRepository.findByReporterIdOrderByCreatedAtDesc(currentUser.getId()).size());
            summary.setUserActiveMatches(itemMatchRepository.findMatchesByUserId(currentUser.getId()).size());
            summary.setUserPendingClaims(claimRepository.countByStatus(ClaimStatus.PENDING));
            summary.setUserRecoveredCount(itemRepository.findByReporterIdOrderByCreatedAtDesc(currentUser.getId())
                    .stream().filter(i -> i.getStatus() == ItemStatus.RECOVERED).count());
        }

        return summary;
    }
}

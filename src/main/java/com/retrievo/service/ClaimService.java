package com.retrievo.service;

import com.retrievo.entity.*;
import com.retrievo.repository.ClaimRepository;
import com.retrievo.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClaimService {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Claim createClaim(Long itemId, User claimant, String proofDescription) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found."));

        if (item.getReporter().getId().equals(claimant.getId())) {
            throw new IllegalArgumentException("You cannot claim an item you reported yourself.");
        }

        Claim claim = new Claim(item, claimant, proofDescription);
        Claim saved = claimRepository.save(claim);

        item.setStatus(ItemStatus.CLAIM_PENDING);
        itemRepository.save(item);

        // Notify item reporter
        notificationService.createNotification(
                item.getReporter(),
                "New Claim Submitted",
                claimant.getFullName() + " submitted an ownership claim for: " + item.getTitle(),
                "CLAIM",
                "/dashboard/claims"
        );

        return saved;
    }

    public List<Claim> getUserClaims(User user) {
        return claimRepository.findAllUserClaims(user.getId());
    }

    @Transactional
    public Claim updateClaimStatus(Long claimId, ClaimStatus status, User reviewer) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found."));

        Item item = claim.getItem();
        if (!item.getReporter().getId().equals(reviewer.getId()) && reviewer.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Unauthorized to review this claim.");
        }

        claim.setStatus(status);
        Claim saved = claimRepository.save(claim);

        if (status == ClaimStatus.APPROVED) {
            item.setStatus(ItemStatus.CLAIM_APPROVED);
            itemRepository.save(item);

            notificationService.createNotification(
                    claim.getClaimant(),
                    "Claim Approved!",
                    "Your claim for " + item.getTitle() + " was approved by finder.",
                    "CLAIM",
                    "/items/" + item.getId()
            );
        } else if (status == ClaimStatus.REJECTED) {
            item.setStatus(ItemStatus.OPEN);
            itemRepository.save(item);

            notificationService.createNotification(
                    claim.getClaimant(),
                    "Claim Update",
                    "Your claim for " + item.getTitle() + " was not approved.",
                    "CLAIM",
                    "/items/" + item.getId()
            );
        }

        return saved;
    }
}

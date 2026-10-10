package com.retrievo.controller;

import com.retrievo.entity.Claim;
import com.retrievo.entity.ClaimStatus;
import com.retrievo.entity.User;
import com.retrievo.service.ClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ClaimController {

    @Autowired
    private ClaimService claimService;

    @PostMapping("/items/{itemId}/claims")
    public ResponseEntity<Claim> createClaim(
            @PathVariable Long itemId,
            @AuthenticationPrincipal User claimant,
            @RequestBody Map<String, String> body
    ) {
        String proof = body.get("proofDescription");
        return ResponseEntity.ok(claimService.createClaim(itemId, claimant, proof));
    }

    @GetMapping("/claims")
    public ResponseEntity<List<Claim>> getClaims(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(claimService.getUserClaims(user));
    }

    @PatchMapping("/claims/{id}")
    public ResponseEntity<Claim> updateClaimStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> body
    ) {
        ClaimStatus status = ClaimStatus.valueOf(body.get("status").toUpperCase());
        return ResponseEntity.ok(claimService.updateClaimStatus(id, status, user));
    }
}

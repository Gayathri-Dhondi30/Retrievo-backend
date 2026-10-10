package com.retrievo.controller;

import com.retrievo.entity.ItemMatch;
import com.retrievo.entity.User;
import com.retrievo.repository.ItemMatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    @Autowired
    private ItemMatchRepository itemMatchRepository;

    @GetMapping
    public ResponseEntity<List<ItemMatch>> getMatchesForQueue(@RequestParam(required = false) Long itemId) {
        if (itemId != null) {
            return ResponseEntity.ok(itemMatchRepository.findMatchesByItemId(itemId));
        }
        return ResponseEntity.ok(itemMatchRepository.findAll());
    }

    @GetMapping("/my-matches")
    public ResponseEntity<List<ItemMatch>> getMyMatches(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(itemMatchRepository.findMatchesByUserId(user.getId()));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<Map<String, String>> acceptMatch(@PathVariable Long id, @AuthenticationPrincipal User user) {
        ItemMatch match = itemMatchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Match not found."));
        match.setStatus("CONFIRMED");
        itemMatchRepository.save(match);
        return ResponseEntity.ok(Map.of("message", "Match accepted successfully."));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, String>> rejectMatch(@PathVariable Long id, @AuthenticationPrincipal User user) {
        ItemMatch match = itemMatchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Match not found."));
        match.setStatus("REJECTED");
        itemMatchRepository.save(match);
        return ResponseEntity.ok(Map.of("message", "Match rejected/dismissed."));
    }
}

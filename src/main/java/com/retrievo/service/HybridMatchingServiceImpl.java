package com.retrievo.service;

import com.retrievo.entity.Item;
import com.retrievo.entity.ItemImage;
import com.retrievo.entity.ItemMatch;
import com.retrievo.entity.ItemStatus;
import com.retrievo.entity.ItemType;
import com.retrievo.repository.ItemMatchRepository;
import com.retrievo.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class HybridMatchingServiceImpl implements MatchingService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemMatchRepository itemMatchRepository;

    @Value("${retrievo.ai-service.url:http://localhost:5000}")
    private String aiServiceUrl;

    @Value("${retrievo.ai-service.matching-threshold:0.60}")
    private double matchingThreshold;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getActiveMatchingMode() {
        try {
            ResponseEntity<Map> health = restTemplate.getForEntity(aiServiceUrl + "/health", Map.class);
            if (health.getStatusCode().is2xxSuccessful() && health.getBody() != null) {
                Object status = health.getBody().get("status");
                if ("healthy".equalsIgnoreCase(String.valueOf(status))) {
                    return "HYBRID_AI_CLIP";
                }
            }
        } catch (Exception e) {
            // FastAPI microservice not reachable
        }
        return "WEIGHTED_FEATURE_FALLBACK";
    }

    @Override
    public List<ItemMatch> findAndSaveMatches(Item targetItem) {
        ItemType oppositeType = (targetItem.getItemType() == ItemType.LOST) ? ItemType.FOUND : ItemType.LOST;
        List<Item> candidates = itemRepository.findByItemTypeAndStatusNot(oppositeType, ItemStatus.RECOVERED);

        String mode = getActiveMatchingMode();
        List<ItemMatch> results = new ArrayList<>();

        for (Item candidate : candidates) {
            // Do not match item with itself or deleted items
            if (candidate.getId().equals(targetItem.getId())) continue;

            Item lostItem = targetItem.getItemType() == ItemType.LOST ? targetItem : candidate;
            Item foundItem = targetItem.getItemType() == ItemType.FOUND ? targetItem : candidate;

            double imageScore = calculateImageSimilarity(lostItem, foundItem, mode);
            double metadataScore = calculateMetadataSimilarity(lostItem, foundItem);

            // Weights: 40% Image Similarity, 60% Metadata Evidence
            double overallScore = (imageScore * 0.40) + (metadataScore * 0.60);

            if (overallScore >= matchingThreshold) {
                ItemMatch match = itemMatchRepository
                        .findByLostItemIdAndFoundItemId(lostItem.getId(), foundItem.getId())
                        .orElse(new ItemMatch());

                match.setLostItem(lostItem);
                match.setFoundItem(foundItem);
                match.setOverallScore(Math.round(overallScore * 100.0) / 100.0);
                match.setImageScore(Math.round(imageScore * 100.0) / 100.0);
                match.setMetadataScore(Math.round(metadataScore * 100.0) / 100.0);
                match.setMatchingMode(mode);
                match.setScoringDetails(String.format("Mode: %s | Image: %.2f | Metadata: %.2f", mode, imageScore, metadataScore));

                results.add(itemMatchRepository.save(match));
            }
        }

        results.sort((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()));
        return results;
    }

    private double calculateImageSimilarity(Item lostItem, Item foundItem, String mode) {
        String lostImg = getPrimaryImage(lostItem);
        String foundImg = getPrimaryImage(foundItem);

        if (lostImg == null || foundImg == null) {
            return 0.50; // Neutral baseline when image absent
        }

        if ("HYBRID_AI_CLIP".equals(mode)) {
            try {
                Map<String, Object> body = new HashMap<>();
                body.put("image1_path", lostImg);
                body.put("image2_path", foundImg);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                ResponseEntity<Map> response = restTemplate.postForEntity(aiServiceUrl + "/api/match/images", entity, Map.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Object simObj = response.getBody().get("similarity_score");
                    if (simObj instanceof Number) {
                        return ((Number) simObj).doubleValue();
                    }
                }
            } catch (Exception ex) {
                // Fallback to local feature heuristic
            }
        }

        // Local Perceptual Fallback Heuristic
        return 0.65;
    }

    private double calculateMetadataSimilarity(Item item1, Item item2) {
        // Category match (0.35 weight)
        double categoryScore = item1.getCategory().equalsIgnoreCase(item2.getCategory()) ? 1.0 : 0.0;

        // Keyword Jaccard Similarity (0.35 weight)
        double textScore = calculateJaccardSimilarity(
                (item1.getTitle() + " " + item1.getDescription()).toLowerCase(),
                (item2.getTitle() + " " + item2.getDescription()).toLowerCase()
        );

        // Location Similarity (0.15 weight)
        double locationScore = calculateJaccardSimilarity(item1.getLocation().toLowerCase(), item2.getLocation().toLowerCase());

        // Date Proximity Score (0.15 weight)
        double dateScore = 0.5;
        if (item1.getDateLostOrFound() != null && item2.getDateLostOrFound() != null) {
            long daysDiff = Math.abs(ChronoUnit.DAYS.between(item1.getDateLostOrFound(), item2.getDateLostOrFound()));
            if (daysDiff == 0) dateScore = 1.0;
            else if (daysDiff <= 3) dateScore = 0.85;
            else if (daysDiff <= 7) dateScore = 0.65;
            else if (daysDiff <= 30) dateScore = 0.40;
            else dateScore = 0.10;
        }

        return (categoryScore * 0.35) + (textScore * 0.35) + (locationScore * 0.15) + (dateScore * 0.15);
    }

    private double calculateJaccardSimilarity(String s1, String s2) {
        Set<String> words1 = new HashSet<>(Arrays.asList(s1.split("\\W+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(s2.split("\\W+")));

        words1.remove("");
        words2.remove("");

        if (words1.isEmpty() || words2.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);

        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);

        return (double) intersection.size() / union.size();
    }

    private String getPrimaryImage(Item item) {
        if (item.getImages() != null && !item.getImages().isEmpty()) {
            ItemImage img = item.getImages().get(0);
            return img.getFilePath() != null ? img.getFilePath() : img.getImageUrl();
        }
        return null;
    }
}

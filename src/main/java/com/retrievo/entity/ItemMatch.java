package com.retrievo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "item_matches", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"lost_item_id", "found_item_id"})
})
public class ItemMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lost_item_id", nullable = false)
    private Item lostItem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "found_item_id", nullable = false)
    private Item foundItem;

    @Column(name = "overall_score", nullable = false)
    private Double overallScore;

    @Column(name = "image_score")
    private Double imageScore;

    @Column(name = "metadata_score")
    private Double metadataScore;

    @Column(nullable = false)
    private String status = "SUGGESTED"; // SUGGESTED, CONFIRMED, REJECTED, DISMISSED

    @Column(name = "scoring_details", columnDefinition = "TEXT")
    private String scoringDetails;

    @Column(name = "matching_mode")
    private String matchingMode = "HYBRID_WEIGHTED";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ItemMatch() {}

    public ItemMatch(Item lostItem, Item foundItem, Double overallScore, Double imageScore, Double metadataScore, String matchingMode) {
        this.lostItem = lostItem;
        this.foundItem = foundItem;
        this.overallScore = overallScore;
        this.imageScore = imageScore;
        this.metadataScore = metadataScore;
        this.matchingMode = matchingMode;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Item getLostItem() { return lostItem; }
    public void setLostItem(Item lostItem) { this.lostItem = lostItem; }

    public Item getFoundItem() { return foundItem; }
    public void setFoundItem(Item foundItem) { this.foundItem = foundItem; }

    public Double getOverallScore() { return overallScore; }
    public void setOverallScore(Double overallScore) { this.overallScore = overallScore; }

    public Double getImageScore() { return imageScore; }
    public void setImageScore(Double imageScore) { this.imageScore = imageScore; }

    public Double getMetadataScore() { return metadataScore; }
    public void setMetadataScore(Double metadataScore) { this.metadataScore = metadataScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getScoringDetails() { return scoringDetails; }
    public void setScoringDetails(String scoringDetails) { this.scoringDetails = scoringDetails; }

    public String getMatchingMode() { return matchingMode; }
    public void setMatchingMode(String matchingMode) { this.matchingMode = matchingMode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

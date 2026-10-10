package com.retrievo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false)
    private String category;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @NotBlank
    @Column(nullable = false)
    private String location;

    @Column(name = "date_lost_or_found")
    private LocalDate dateLostOrFound;

    @Column(name = "approximate_time")
    private String approximateTime;

    @Column(name = "distinguishing_features", columnDefinition = "TEXT")
    private String distinguishingFeatures;

    @Column(name = "safe_storage_notes")
    private String safeStorageNotes;

    @Column(name = "contact_preference")
    private String contactPreference = "IN_APP_MESSAGE";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status = ItemStatus.OPEN;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemImage> images = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Item() {}

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addImage(ItemImage image) {
        images.add(image);
        image.setItem(this);
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ItemType getItemType() { return itemType; }
    public void setItemType(ItemType itemType) { this.itemType = itemType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getDateLostOrFound() { return dateLostOrFound; }
    public void setDateLostOrFound(LocalDate dateLostOrFound) { this.dateLostOrFound = dateLostOrFound; }

    public String getApproximateTime() { return approximateTime; }
    public void setApproximateTime(String approximateTime) { this.approximateTime = approximateTime; }

    public String getDistinguishingFeatures() { return distinguishingFeatures; }
    public void setDistinguishingFeatures(String distinguishingFeatures) { this.distinguishingFeatures = distinguishingFeatures; }

    public String getSafeStorageNotes() { return safeStorageNotes; }
    public void setSafeStorageNotes(String safeStorageNotes) { this.safeStorageNotes = safeStorageNotes; }

    public String getContactPreference() { return contactPreference; }
    public void setContactPreference(String contactPreference) { this.contactPreference = contactPreference; }

    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }

    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }

    public List<ItemImage> getImages() { return images; }
    public void setImages(List<ItemImage> images) { this.images = images; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

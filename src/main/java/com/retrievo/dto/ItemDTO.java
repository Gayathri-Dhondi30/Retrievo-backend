package com.retrievo.dto;

import com.retrievo.entity.Item;
import com.retrievo.entity.ItemStatus;
import com.retrievo.entity.ItemType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ItemDTO {
    private Long id;
    private ItemType itemType;
    private String title;
    private String category;
    private String description;
    private String location;
    private LocalDate dateLostOrFound;
    private String approximateTime;
    private String distinguishingFeatures;
    private String safeStorageNotes;
    private String contactPreference;
    private ItemStatus status;
    private Long reporterId;
    private String reporterName;
    private List<ImageDTO> images = new ArrayList<>();
    private LocalDateTime createdAt;

    public ItemDTO() {}

    public ItemDTO(Item item) {
        if (item != null) {
            this.id = item.getId();
            this.itemType = item.getItemType();
            this.title = item.getTitle();
            this.category = item.getCategory();
            this.description = item.getDescription();
            this.location = item.getLocation();
            this.dateLostOrFound = item.getDateLostOrFound();
            this.approximateTime = item.getApproximateTime();
            this.distinguishingFeatures = item.getDistinguishingFeatures();
            this.safeStorageNotes = item.getSafeStorageNotes();
            this.contactPreference = item.getContactPreference();
            this.status = item.getStatus();
            if (item.getReporter() != null) {
                this.reporterId = item.getReporter().getId();
                this.reporterName = item.getReporter().getFullName();
            }
            if (item.getImages() != null) {
                item.getImages().forEach(img -> this.images.add(new ImageDTO(img.getId(), img.getImageUrl(), img.getFilePath(), img.getFileName())));
            }
            this.createdAt = item.getCreatedAt();
        }
    }

    public static class ImageDTO {
        private Long id;
        private String imageUrl;
        private String filePath;
        private String fileName;

        public ImageDTO() {}
        public ImageDTO(Long id, String imageUrl, String filePath, String fileName) {
            this.id = id;
            this.imageUrl = imageUrl;
            this.filePath = filePath;
            this.fileName = fileName;
        }

        public Long getId() { return id; }
        public String getImageUrl() { return imageUrl; }
        public String getFilePath() { return filePath; }
        public String getFileName() { return fileName; }
    }

    // Getters and Setters
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

    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public List<ImageDTO> getImages() { return images; }
    public void setImages(List<ImageDTO> images) { this.images = images; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

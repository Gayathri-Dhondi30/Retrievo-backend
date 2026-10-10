package com.retrievo.service;

import com.retrievo.dto.ItemDTO;
import com.retrievo.entity.*;
import com.retrievo.repository.ItemImageRepository;
import com.retrievo.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemImageRepository itemImageRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private MatchingService matchingService;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public ItemDTO createItem(
            User reporter,
            ItemType itemType,
            String title,
            String category,
            String description,
            String location,
            String dateLostOrFound,
            String approximateTime,
            String distinguishingFeatures,
            String safeStorageNotes,
            String contactPreference,
            MultipartFile imageFile
    ) {
        Item item = new Item();
        item.setReporter(reporter);
        item.setItemType(itemType);
        item.setTitle(title);
        item.setCategory(category);
        item.setDescription(description);
        item.setLocation(location);
        if (dateLostOrFound != null && !dateLostOrFound.isBlank()) {
            item.setDateLostOrFound(LocalDate.parse(dateLostOrFound));
        }
        item.setApproximateTime(approximateTime);
        item.setDistinguishingFeatures(distinguishingFeatures);
        item.setSafeStorageNotes(safeStorageNotes);
        if (contactPreference != null) {
            item.setContactPreference(contactPreference);
        }
        item.setStatus(ItemStatus.OPEN);

        Item savedItem = itemRepository.save(item);

        // Process image upload if provided
        if (imageFile != null && !imageFile.isEmpty()) {
            String safeFileName = fileStorageService.storeFile(imageFile);
            String imageUrl = "/uploads/" + safeFileName;
            ItemImage img = new ItemImage(savedItem, imageUrl, safeFileName, safeFileName, imageFile.getSize(), imageFile.getContentType());
            itemImageRepository.save(img);
            savedItem.addImage(img);
        }

        // Trigger AI matching pipeline in background asynchronously
        try {
            List<ItemMatch> matches = matchingService.findAndSaveMatches(savedItem);
            if (!matches.isEmpty()) {
                savedItem.setStatus(ItemStatus.MATCHED);
                itemRepository.save(savedItem);
                notificationService.createNotification(
                        reporter,
                        "AI Match Discovered!",
                        "We discovered " + matches.size() + " potential match(es) for your item: " + title,
                        "MATCH",
                        "/items/" + savedItem.getId()
                );
            }
        } catch (Exception e) {
            // Log matching error without blocking item creation
        }

        return new ItemDTO(savedItem);
    }

    public Page<ItemDTO> searchItems(
            String typeStr,
            String category,
            String statusStr,
            String location,
            String startDateStr,
            String keyword,
            int page,
            int size,
            String sortOrder
    ) {
        ItemType itemType = typeStr != null && !typeStr.equalsIgnoreCase("ALL") ? ItemType.valueOf(typeStr.toUpperCase()) : null;
        ItemStatus status = statusStr != null && !statusStr.isBlank() ? ItemStatus.valueOf(statusStr.toUpperCase()) : null;
        LocalDate startDate = startDateStr != null && !startDateStr.isBlank() ? LocalDate.parse(startDateStr) : null;

        Sort sort = "oldest".equalsIgnoreCase(sortOrder)
                ? Sort.by("createdAt").ascending()
                : Sort.by("createdAt").descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Item> itemsPage = itemRepository.findWithFilters(
                itemType,
                category != null && category.isBlank() ? null : category,
                status,
                location != null && location.isBlank() ? null : location,
                startDate,
                keyword != null && keyword.isBlank() ? null : keyword,
                pageable
        );

        return itemsPage.map(ItemDTO::new);
    }

    public ItemDTO getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + id));
        return new ItemDTO(item);
    }

    public List<ItemDTO> getMyItems(User user) {
        List<Item> items = itemRepository.findByReporterIdOrderByCreatedAtDesc(user.getId());
        return items.stream().map(ItemDTO::new).toList();
    }

    @Transactional
    public void deleteItem(Long id, User user) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found."));

        if (!item.getReporter().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Unauthorized to delete this report.");
        }

        itemRepository.delete(item);
    }

    @Transactional
    public ItemDTO updateItemStatus(Long id, ItemStatus status, User user) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found."));

        if (!item.getReporter().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Unauthorized to modify status.");
        }

        item.setStatus(status);
        Item updated = itemRepository.save(item);
        return new ItemDTO(updated);
    }
}

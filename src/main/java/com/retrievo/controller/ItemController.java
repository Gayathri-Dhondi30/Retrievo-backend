package com.retrievo.controller;

import com.retrievo.dto.ItemDTO;
import com.retrievo.entity.ItemStatus;
import com.retrievo.entity.ItemType;
import com.retrievo.entity.User;
import com.retrievo.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<Page<ItemDTO>> searchItems(
            @RequestParam(required = false) String itemType,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "newest") String sort
    ) {
        return ResponseEntity.ok(itemService.searchItems(itemType, category, status, location, startDate, keyword, page, size, sort));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @GetMapping("/my-items")
    public ResponseEntity<List<ItemDTO>> getMyItems(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(itemService.getMyItems(user));
    }

    @PostMapping("/lost")
    public ResponseEntity<ItemDTO> createLostItem(
            @AuthenticationPrincipal User user,
            @RequestParam("title") String title,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam(value = "dateLostOrFound", required = false) String dateLostOrFound,
            @RequestParam(value = "approximateTime", required = false) String approximateTime,
            @RequestParam(value = "distinguishingFeatures", required = false) String distinguishingFeatures,
            @RequestParam(value = "contactPreference", required = false) String contactPreference,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        ItemDTO created = itemService.createItem(
                user, ItemType.LOST, title, category, description, location,
                dateLostOrFound, approximateTime, distinguishingFeatures, null, contactPreference, image
        );
        return ResponseEntity.ok(created);
    }

    @PostMapping("/found")
    public ResponseEntity<ItemDTO> createFoundItem(
            @AuthenticationPrincipal User user,
            @RequestParam("title") String title,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam(value = "dateLostOrFound", required = false) String dateLostOrFound,
            @RequestParam(value = "approximateTime", required = false) String approximateTime,
            @RequestParam(value = "distinguishingFeatures", required = false) String distinguishingFeatures,
            @RequestParam(value = "safeStorageNotes", required = false) String safeStorageNotes,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        ItemDTO created = itemService.createItem(
                user, ItemType.FOUND, title, category, description, location,
                dateLostOrFound, approximateTime, distinguishingFeatures, safeStorageNotes, "IN_APP_MESSAGE", image
        );
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteItem(@PathVariable Long id, @AuthenticationPrincipal User user) {
        itemService.deleteItem(id, user);
        return ResponseEntity.ok(Map.of("message", "Item deleted successfully."));
    }

    @PostMapping("/{id}/confirm-recovery")
    public ResponseEntity<ItemDTO> confirmRecovery(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(itemService.updateItemStatus(id, ItemStatus.RECOVERED, user));
    }
}

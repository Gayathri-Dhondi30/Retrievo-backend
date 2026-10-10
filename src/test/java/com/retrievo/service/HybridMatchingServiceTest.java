package com.retrievo.service;

import com.retrievo.entity.Item;
import com.retrievo.entity.ItemMatch;
import com.retrievo.entity.ItemType;
import com.retrievo.repository.ItemMatchRepository;
import com.retrievo.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class HybridMatchingServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMatchRepository itemMatchRepository;

    @InjectMocks
    private HybridMatchingServiceImpl matchingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testActiveMatchingModeFallback() {
        String mode = matchingService.getActiveMatchingMode();
        assertNotNull(mode);
        assertTrue(mode.equals("WEIGHTED_FEATURE_FALLBACK") || mode.equals("HYBRID_AI_CLIP"));
    }
}

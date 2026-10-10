package com.retrievo.service;

import com.retrievo.entity.Item;
import com.retrievo.entity.ItemMatch;
import java.util.List;

public interface MatchingService {
    List<ItemMatch> findAndSaveMatches(Item targetItem);
    String getActiveMatchingMode();
}

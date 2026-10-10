package com.retrievo.repository;

import com.retrievo.entity.ItemMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemMatchRepository extends JpaRepository<ItemMatch, Long> {

    Optional<ItemMatch> findByLostItemIdAndFoundItemId(Long lostItemId, Long foundItemId);

    @Query("SELECT m FROM ItemMatch m WHERE m.lostItem.id = :itemId OR m.foundItem.id = :itemId ORDER BY m.overallScore DESC")
    List<ItemMatch> findMatchesByItemId(@Param("itemId") Long itemId);

    @Query("SELECT m FROM ItemMatch m WHERE (m.lostItem.reporter.id = :userId OR m.foundItem.reporter.id = :userId) AND m.status != 'REJECTED' ORDER BY m.overallScore DESC")
    List<ItemMatch> findMatchesByUserId(@Param("userId") Long userId);
}

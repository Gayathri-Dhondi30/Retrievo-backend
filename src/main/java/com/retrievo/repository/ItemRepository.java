package com.retrievo.repository;

import com.retrievo.entity.Item;
import com.retrievo.entity.ItemStatus;
import com.retrievo.entity.ItemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByReporterIdOrderByCreatedAtDesc(Long reporterId);

    long countByItemType(ItemType itemType);

    long countByStatus(ItemStatus status);

    List<Item> findByItemTypeAndStatusNot(ItemType itemType, ItemStatus status);

    @Query("SELECT i FROM Item i WHERE " +
           "(:itemType IS NULL OR i.itemType = :itemType) AND " +
           "(:category IS NULL OR LOWER(i.category) = LOWER(:category)) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:location IS NULL OR LOWER(i.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:startDate IS NULL OR i.dateLostOrFound >= :startDate) AND " +
           "(:keyword IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Item> findWithFilters(
            @Param("itemType") ItemType itemType,
            @Param("category") String category,
            @Param("status") ItemStatus status,
            @Param("location") String location,
            @Param("startDate") LocalDate startDate,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}

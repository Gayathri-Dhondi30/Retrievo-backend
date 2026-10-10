package com.retrievo.repository;

import com.retrievo.entity.Claim;
import com.retrievo.entity.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByItemId(Long itemId);

    List<Claim> findByClaimantIdOrderByCreatedAtDesc(Long claimantId);

    @Query("SELECT c FROM Claim c WHERE c.item.reporter.id = :reporterId OR c.claimant.id = :reporterId ORDER BY c.createdAt DESC")
    List<Claim> findAllUserClaims(@Param("reporterId") Long reporterId);

    long countByStatus(ClaimStatus status);
}

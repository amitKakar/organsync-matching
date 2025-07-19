package com.organsync.matching.repository;

import com.organsync.matching.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, UUID> {

    List<Match> findByStatus(String status);

    List<Match> findByHospitalId(String hospitalId);

    List<Match> findByMatchType(String matchType);

    @Query("SELECT m FROM Match m WHERE m.compatibilityScore >= :minScore")
    List<Match> findByMinimumCompatibilityScore(@Param("minScore") Double minScore);

    @Query("SELECT m FROM Match m WHERE m.createdAt BETWEEN :startDate AND :endDate")
    List<Match> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT m FROM Match m WHERE :pairId MEMBER OF m.pairIds")
    List<Match> findByPairId(@Param("pairId") UUID pairId);

    @Query("SELECT COUNT(m) FROM Match m WHERE m.status = :status")
    Long countByStatus(@Param("status") String status);

    @Query("SELECT AVG(m.compatibilityScore) FROM Match m WHERE m.status = 'COMPLETED'")
    Double averageCompatibilityScore();

    @Query("SELECT m FROM Match m WHERE m.status = 'PENDING' AND m.priorityLevel IS NOT NULL ORDER BY m.priorityLevel DESC")
    List<Match> findPendingMatchesOrderedByPriority();
}
package com.organsync.matching.repository;

import com.organsync.matching.entity.Compatibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompatibilityRepository extends JpaRepository<Compatibility, UUID> {

    List<Compatibility> findByDonorPairId(UUID donorPairId);

    List<Compatibility> findByRecipientPairId(UUID recipientPairId);

    @Query("SELECT c FROM Compatibility c WHERE c.bloodTypeCompatible = true AND c.hlaCompatible = true AND c.crossmatchCompatible = true")
    List<Compatibility> findFullyCompatible();

    @Query("SELECT c FROM Compatibility c WHERE c.compatibilityScore >= :minScore")
    List<Compatibility> findByMinimumScore(@Param("minScore") Double minScore);

    @Query("SELECT c FROM Compatibility c WHERE c.distanceKm <= :maxDistance")
    List<Compatibility> findByMaxDistance(@Param("maxDistance") Double maxDistance);

    @Query("SELECT c FROM Compatibility c WHERE c.donorPairId = :donorId AND c.recipientPairId = :recipientId")
    Compatibility findByDonorAndRecipient(@Param("donorId") UUID donorId, 
                                         @Param("recipientId") UUID recipientId);

    @Query("SELECT COUNT(c) FROM Compatibility c WHERE c.bloodTypeCompatible = true")
    Long countBloodTypeCompatible();

    @Query("SELECT AVG(c.compatibilityScore) FROM Compatibility c WHERE c.bloodTypeCompatible = true")
    Double averageCompatibilityScore();
}
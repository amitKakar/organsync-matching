package com.organsync.matching.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Compatibility entity representing compatibility between donor-recipient pairs
 */
@Entity
@Table(name = "compatibility")
public class Compatibility {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @Column(name = "donor_pair_id")
    private UUID donorPairId;

    @NotNull
    @Column(name = "recipient_pair_id")
    private UUID recipientPairId;

    @Column(name = "blood_type_compatible")
    private Boolean bloodTypeCompatible;

    @Column(name = "hla_compatible")
    private Boolean hlaCompatible;

    @Column(name = "crossmatch_compatible")
    private Boolean crossmatchCompatible;

    @Column(name = "compatibility_score")
    private Double compatibilityScore;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Compatibility() {}

    public Compatibility(UUID donorPairId, UUID recipientPairId, 
                        Boolean bloodTypeCompatible, Boolean hlaCompatible,
                        Boolean crossmatchCompatible, Double compatibilityScore) {
        this.donorPairId = donorPairId;
        this.recipientPairId = recipientPairId;
        this.bloodTypeCompatible = bloodTypeCompatible;
        this.hlaCompatible = hlaCompatible;
        this.crossmatchCompatible = crossmatchCompatible;
        this.compatibilityScore = compatibilityScore;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getDonorPairId() { return donorPairId; }
    public void setDonorPairId(UUID donorPairId) { this.donorPairId = donorPairId; }

    public UUID getRecipientPairId() { return recipientPairId; }
    public void setRecipientPairId(UUID recipientPairId) { this.recipientPairId = recipientPairId; }

    public Boolean getBloodTypeCompatible() { return bloodTypeCompatible; }
    public void setBloodTypeCompatible(Boolean bloodTypeCompatible) { this.bloodTypeCompatible = bloodTypeCompatible; }

    public Boolean getHlaCompatible() { return hlaCompatible; }
    public void setHlaCompatible(Boolean hlaCompatible) { this.hlaCompatible = hlaCompatible; }

    public Boolean getCrossmatchCompatible() { return crossmatchCompatible; }
    public void setCrossmatchCompatible(Boolean crossmatchCompatible) { this.crossmatchCompatible = crossmatchCompatible; }

    public Double getCompatibilityScore() { return compatibilityScore; }
    public void setCompatibilityScore(Double compatibilityScore) { this.compatibilityScore = compatibilityScore; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
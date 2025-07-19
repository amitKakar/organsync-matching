package com.organsync.matching.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Compatibility entity
 */
public class CompatibilityDto {

    private UUID id;

    @NotNull
    @JsonProperty("donor_pair_id")
    private UUID donorPairId;

    @NotNull
    @JsonProperty("recipient_pair_id")
    private UUID recipientPairId;

    @JsonProperty("blood_type_compatible")
    private Boolean bloodTypeCompatible;

    @JsonProperty("hla_compatible")
    private Boolean hlaCompatible;

    @JsonProperty("crossmatch_compatible")
    private Boolean crossmatchCompatible;

    @JsonProperty("compatibility_score")
    private Double compatibilityScore;

    @JsonProperty("distance_km")
    private Double distanceKm;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public CompatibilityDto() {}

    public CompatibilityDto(UUID donorPairId, UUID recipientPairId, 
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
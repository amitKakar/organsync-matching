package com.organsync.matching.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for Match entity
 */
public class MatchDto {

    private UUID id;

    @NotNull
    @JsonProperty("match_type")
    private String matchType;

    @NotNull
    @JsonProperty("status")
    private String status;

    @NotNull
    @JsonProperty("pair_ids")
    private List<UUID> pairIds;

    @JsonProperty("compatibility_score")
    private Double compatibilityScore;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("hospital_id")
    private String hospitalId;

    @JsonProperty("priority_level")
    private Integer priorityLevel;

    // Constructors
    public MatchDto() {}

    public MatchDto(String matchType, String status, List<UUID> pairIds, 
                   Double compatibilityScore, String hospitalId) {
        this.matchType = matchType;
        this.status = status;
        this.pairIds = pairIds;
        this.compatibilityScore = compatibilityScore;
        this.hospitalId = hospitalId;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<UUID> getPairIds() { return pairIds; }
    public void setPairIds(List<UUID> pairIds) { this.pairIds = pairIds; }

    public Double getCompatibilityScore() { return compatibilityScore; }
    public void setCompatibilityScore(Double compatibilityScore) { this.compatibilityScore = compatibilityScore; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public Integer getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(Integer priorityLevel) { this.priorityLevel = priorityLevel; }
}
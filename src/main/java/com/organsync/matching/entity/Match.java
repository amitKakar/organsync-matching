package com.organsync.matching.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Match entity representing a kidney exchange match between donor-recipient pairs
 */
@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @Column(name = "match_type")
    @Enumerated(EnumType.STRING)
    private MatchType matchType;

    @NotNull
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    @ElementCollection
    @CollectionTable(name = "match_pairs", joinColumns = @JoinColumn(name = "match_id"))
    @Column(name = "pair_id")
    private List<UUID> pairIds;

    @Column(name = "compatibility_score")
    private Double compatibilityScore;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "hospital_id")
    private String hospitalId;

    @Column(name = "priority_level")
    private Integer priorityLevel;

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
    public Match() {}

    public Match(MatchType matchType, MatchStatus status, List<UUID> pairIds, 
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

    public MatchType getMatchType() { return matchType; }
    public void setMatchType(MatchType matchType) { this.matchType = matchType; }

    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }

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
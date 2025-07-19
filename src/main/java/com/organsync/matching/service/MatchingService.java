package com.organsync.matching.service;

import com.organsync.matching.algorithm.MatchingAlgorithmService;
import com.organsync.matching.dto.CompatibilityDto;
import com.organsync.matching.dto.MatchDto;
import com.organsync.matching.entity.Compatibility;
import com.organsync.matching.entity.Match;
import com.organsync.matching.entity.MatchStatus;
import com.organsync.matching.entity.MatchType;
import com.organsync.matching.repository.CompatibilityRepository;
import com.organsync.matching.repository.MatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Core matching service for kidney exchange operations
 */
@Service
@Transactional
public class MatchingService {

    private static final Logger logger = LoggerFactory.getLogger(MatchingService.class);

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private CompatibilityRepository compatibilityRepository;

    @Autowired
    private MatchingAlgorithmService algorithmService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Process new pair registration event and find matches
     */
    public void processPairRegistration(UUID pairId) {
        logger.info("Processing pair registration for pair ID: {}", pairId);

        // Calculate compatibility with existing pairs
        List<Compatibility> newCompatibilities = calculateCompatibilityForPair(pairId);

        // Save new compatibility relationships
        compatibilityRepository.saveAll(newCompatibilities);

        // Find new matches
        List<MatchDto> newMatches = findNewMatches();

        // Save and publish matches
        for (MatchDto matchDto : newMatches) {
            Match match = convertToEntity(matchDto);
            Match savedMatch = matchRepository.save(match);

            // Publish match found event
            publishMatchFoundEvent(convertToDto(savedMatch));
        }

        logger.info("Processed pair registration, found {} new matches", newMatches.size());
    }

    /**
     * Find all available matches using multiple algorithms
     */
    public List<MatchDto> findAllMatches() {
        logger.info("Finding all available matches");

        List<Compatibility> compatibilities = compatibilityRepository.findFullyCompatible();
        List<MatchDto> allMatches = new ArrayList<>();

        // Find optimal matches using Edmonds' Blossom Algorithm
        allMatches.addAll(algorithmService.findOptimalMatches(compatibilities));

        // Find cycles
        allMatches.addAll(algorithmService.findCycles(compatibilities, 3));

        // Find chains (assuming we have altruistic donors)
        List<UUID> altruisticDonors = getAltruisticDonors();
        allMatches.addAll(algorithmService.findChains(compatibilities, altruisticDonors, 5));

        logger.info("Found {} total matches", allMatches.size());
        return allMatches;
    }

    /**
     * Get match by ID
     */
    public Optional<MatchDto> getMatchById(UUID matchId) {
        return matchRepository.findById(matchId).map(this::convertToDto);
    }

    /**
     * Get all matches for a hospital
     */
    public List<MatchDto> getMatchesByHospital(String hospitalId) {
        return matchRepository.findByHospitalId(hospitalId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Update match status
     */
    public MatchDto updateMatchStatus(UUID matchId, String newStatus) {
        logger.info("Updating match {} status to {}", matchId, newStatus);

        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (matchOpt.isPresent()) {
            Match match = matchOpt.get();
            match.setStatus(MatchStatus.PENDING);
            Match savedMatch = matchRepository.save(match);

            // Publish status update event
            publishMatchStatusUpdateEvent(convertToDto(savedMatch));

            return convertToDto(savedMatch);
        }

        throw new IllegalArgumentException("Match not found with ID: " + matchId);
    }

    /**
     * Get match statistics
     */
    public MatchStatistics getMatchStatistics() {
        MatchStatistics stats = new MatchStatistics();

        stats.setTotalMatches(matchRepository.count());
        stats.setPendingMatches(matchRepository.countByStatus("PENDING"));
        stats.setCompletedMatches(matchRepository.countByStatus("COMPLETED"));
        stats.setAverageCompatibilityScore(matchRepository.averageCompatibilityScore());

        return stats;
    }

    /**
     * Calculate compatibility for a new pair
     */
    private List<Compatibility> calculateCompatibilityForPair(UUID pairId) {
        // This would integrate with the registration service to get pair details
        // For now, return empty list - implementation depends on integration
        return new ArrayList<>();
    }

    /**
     * Find new matches after pair registration
     */
    private List<MatchDto> findNewMatches() {
        List<Compatibility> compatibilities = compatibilityRepository.findFullyCompatible();
        return algorithmService.findOptimalMatches(compatibilities);
    }

    /**
     * Get list of altruistic donors
     */
    private List<UUID> getAltruisticDonors() {
        // This would integrate with the registration service
        // For now, return empty list - implementation depends on integration
        return new ArrayList<>();
    }

    /**
     * Publish match found event to Kafka
     */
    private void publishMatchFoundEvent(MatchDto match) {
        try {
            kafkaTemplate.send("match.found", match.getId().toString(), match);
            logger.info("Published match found event for match ID: {}", match.getId());
        } catch (Exception e) {
            logger.error("Failed to publish match found event", e);
        }
    }

    /**
     * Publish match status update event to Kafka
     */
    private void publishMatchStatusUpdateEvent(MatchDto match) {
        try {
            kafkaTemplate.send("match.status.updated", match.getId().toString(), match);
            logger.info("Published match status update event for match ID: {}", match.getId());
        } catch (Exception e) {
            logger.error("Failed to publish match status update event", e);
        }
    }

    /**
     * Convert Match entity to DTO
     */
    private MatchDto convertToDto(Match match) {
        MatchDto dto = new MatchDto();
        dto.setId(match.getId());
        dto.setMatchType(match.getMatchType().toString());
        dto.setStatus(match.getStatus().toString());
        dto.setPairIds(match.getPairIds());
        dto.setCompatibilityScore(match.getCompatibilityScore());
        dto.setCreatedAt(match.getCreatedAt());
        dto.setUpdatedAt(match.getUpdatedAt());
        dto.setHospitalId(match.getHospitalId());
        dto.setPriorityLevel(match.getPriorityLevel());
        return dto;
    }

    /**
     * Convert MatchDto to entity
     */
    private Match convertToEntity(MatchDto dto) {
        Match match = new Match();
        match.setId(dto.getId());
        match.setMatchType(MatchType.DIRECT_EXCHANGE);
        match.setStatus(MatchStatus.CONFIRMED);
        match.setPairIds(dto.getPairIds());
        match.setCompatibilityScore(dto.getCompatibilityScore());
        match.setHospitalId(dto.getHospitalId());
        match.setPriorityLevel(dto.getPriorityLevel());
        return match;
    }

    /**
     * Inner class for match statistics
     */
    public static class MatchStatistics {
        private long totalMatches;
        private long pendingMatches;
        private long completedMatches;
        private Double averageCompatibilityScore;

        // Getters and setters
        public long getTotalMatches() { return totalMatches; }
        public void setTotalMatches(long totalMatches) { this.totalMatches = totalMatches; }

        public long getPendingMatches() { return pendingMatches; }
        public void setPendingMatches(long pendingMatches) { this.pendingMatches = pendingMatches; }

        public long getCompletedMatches() { return completedMatches; }
        public void setCompletedMatches(long completedMatches) { this.completedMatches = completedMatches; }

        public Double getAverageCompatibilityScore() { return averageCompatibilityScore; }
        public void setAverageCompatibilityScore(Double averageCompatibilityScore) { this.averageCompatibilityScore = averageCompatibilityScore; }
    }
}
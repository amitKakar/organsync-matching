package com.organsync.matching.service;

import com.organsync.matching.dto.MatchDto;
import com.organsync.matching.entity.Compatibility;
import com.organsync.matching.entity.Match;
import com.organsync.matching.entity.MatchStatus;
import com.organsync.matching.entity.MatchType;
import com.organsync.matching.repository.CompatibilityRepository;
import com.organsync.matching.repository.MatchRepository;
import com.organsync.matching.algorithm.MatchingAlgorithmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private CompatibilityRepository compatibilityRepository;

    @Mock
    private MatchingAlgorithmService algorithmService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private MatchingService matchingService;

    private UUID testMatchId;
    private UUID testPairId1;
    private UUID testPairId2;
    private Match testMatch;
    private Compatibility testCompatibility;

    @BeforeEach
    void setUp() {
        testMatchId = UUID.randomUUID();
        testPairId1 = UUID.randomUUID();
        testPairId2 = UUID.randomUUID();

        testMatch = new Match();
        testMatch.setId(testMatchId);
        testMatch.setMatchType(MatchType.TWO_WAY_CYCLE);
        testMatch.setStatus(MatchStatus.PENDING);
        testMatch.setPairIds(Arrays.asList(testPairId1, testPairId2));
        testMatch.setCompatibilityScore(0.85);
        testMatch.setHospitalId("HOSPITAL_001");

        testCompatibility = new Compatibility();
        testCompatibility.setId(UUID.randomUUID());
        testCompatibility.setDonorPairId(testPairId1);
        testCompatibility.setRecipientPairId(testPairId2);
        testCompatibility.setBloodTypeCompatible(true);
        testCompatibility.setHlaCompatible(true);
        testCompatibility.setCrossmatchCompatible(true);
        testCompatibility.setCompatibilityScore(0.85);
    }

    @Test
    void testFindAllMatches() {
        // Arrange
        List<Compatibility> compatibilities = Arrays.asList(testCompatibility);
        List<MatchDto> expectedMatches = Arrays.asList(new MatchDto());

        when(compatibilityRepository.findFullyCompatible()).thenReturn(compatibilities);
        when(algorithmService.findOptimalMatches(compatibilities)).thenReturn(expectedMatches);
        when(algorithmService.findCycles(compatibilities, 3)).thenReturn(Arrays.asList());
        when(algorithmService.findChains(eq(compatibilities), any(), eq(5))).thenReturn(Arrays.asList());

        // Act
        List<MatchDto> result = matchingService.findAllMatches();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(compatibilityRepository).findFullyCompatible();
        verify(algorithmService).findOptimalMatches(compatibilities);
    }

    @Test
    void testGetMatchById() {
        // Arrange
        when(matchRepository.findById(testMatchId)).thenReturn(Optional.of(testMatch));

        // Act
        Optional<MatchDto> result = matchingService.getMatchById(testMatchId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testMatchId, result.get().getId());
        assertEquals("TWO_WAY_CYCLE", result.get().getMatchType());
        verify(matchRepository).findById(testMatchId);
    }

    @Test
    void testGetMatchByIdNotFound() {
        // Arrange
        when(matchRepository.findById(testMatchId)).thenReturn(Optional.empty());

        // Act
        Optional<MatchDto> result = matchingService.getMatchById(testMatchId);

        // Assert
        assertFalse(result.isPresent());
        verify(matchRepository).findById(testMatchId);
    }

    @Test
    void testUpdateMatchStatus() {
        // Arrange
        String newStatus = "APPROVED";
        when(matchRepository.findById(testMatchId)).thenReturn(Optional.of(testMatch));
        when(matchRepository.save(any(Match.class))).thenReturn(testMatch);

        // Act
        MatchDto result = matchingService.updateMatchStatus(testMatchId, newStatus);

        // Assert
        assertNotNull(result);
        assertEquals(testMatchId, result.getId());
        verify(matchRepository).findById(testMatchId);
        verify(matchRepository).save(any(Match.class));
        verify(kafkaTemplate).send(eq("match.status.updated"), eq(testMatchId.toString()), any());
    }

    @Test
    void testUpdateMatchStatusNotFound() {
        // Arrange
        String newStatus = "APPROVED";
        when(matchRepository.findById(testMatchId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            matchingService.updateMatchStatus(testMatchId, newStatus);
        });

        verify(matchRepository).findById(testMatchId);
        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    void testGetMatchStatistics() {
        // Arrange
        when(matchRepository.count()).thenReturn(100L);
        when(matchRepository.countByStatus("PENDING")).thenReturn(30L);
        when(matchRepository.countByStatus("COMPLETED")).thenReturn(50L);
        when(matchRepository.averageCompatibilityScore()).thenReturn(0.75);

        // Act
        MatchingService.MatchStatistics result = matchingService.getMatchStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getTotalMatches());
        assertEquals(30L, result.getPendingMatches());
        assertEquals(50L, result.getCompletedMatches());
        assertEquals(0.75, result.getAverageCompatibilityScore());

        verify(matchRepository).count();
        verify(matchRepository).countByStatus("PENDING");
        verify(matchRepository).countByStatus("COMPLETED");
        verify(matchRepository).averageCompatibilityScore();
    }

    @Test
    void testProcessPairRegistration() {
        // Arrange
        UUID pairId = UUID.randomUUID();
        List<MatchDto> newMatches = Arrays.asList(new MatchDto());
        when(algorithmService.findOptimalMatches(any())).thenReturn(newMatches);
        when(matchRepository.save(any(Match.class))).thenReturn(testMatch);

        // Act
        matchingService.processPairRegistration(pairId);

        // Assert
        verify(compatibilityRepository).saveAll(any());
        verify(matchRepository).save(any(Match.class));
        verify(kafkaTemplate).send(eq("match.found"), any(), any());
    }
}
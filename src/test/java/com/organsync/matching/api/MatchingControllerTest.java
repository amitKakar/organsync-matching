package com.organsync.matching.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.organsync.matching.dto.MatchDto;
import com.organsync.matching.service.MatchingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchingController.class)
class MatchingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchingService matchingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/matching/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Matching service is running"));
    }

    @Test
    @WithMockUser(authorities = "SCOPE_match.read")
    void testFindAllMatches() throws Exception {
        List<MatchDto> matches = Arrays.asList(createTestMatchDto());
        when(matchingService.findAllMatches()).thenReturn(matches);

        mockMvc.perform(get("/api/v1/matching/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].match_type").value("TWO_WAY_CYCLE"));

        verify(matchingService).findAllMatches();
    }

    @Test
    @WithMockUser(authorities = "SCOPE_match.read")
    void testGetMatchById() throws Exception {
        UUID matchId = UUID.randomUUID();
        MatchDto match = createTestMatchDto();
        match.setId(matchId);

        when(matchingService.getMatchById(matchId)).thenReturn(Optional.of(match));

        mockMvc.perform(get("/api/v1/matching/matches/{matchId}", matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.match_type").value("TWO_WAY_CYCLE"))
                .andExpect(jsonPath("$.id").value(matchId.toString()));

        verify(matchingService).getMatchById(matchId);
    }

    @Test
    @WithMockUser(authorities = "SCOPE_match.read")
    void testGetMatchByIdNotFound() throws Exception {
        UUID matchId = UUID.randomUUID();
        when(matchingService.getMatchById(matchId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/matching/matches/{matchId}", matchId))
                .andExpect(status().isNotFound());

        verify(matchingService).getMatchById(matchId);
    }

    @Test
    @WithMockUser(authorities = "SCOPE_match.write")
    void testUpdateMatchStatus() throws Exception {
        UUID matchId = UUID.randomUUID();
        MatchDto updatedMatch = createTestMatchDto();
        updatedMatch.setId(matchId);
        updatedMatch.setStatus("APPROVED");

        when(matchingService.updateMatchStatus(eq(matchId), eq("APPROVED"))).thenReturn(updatedMatch);

        MatchingController.StatusUpdateRequest request = new MatchingController.StatusUpdateRequest();
        request.setStatus("APPROVED");

        mockMvc.perform(put("/api/v1/matching/matches/{matchId}/status", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(matchingService).updateMatchStatus(matchId, "APPROVED");
    }

    @Test
    @WithMockUser(authorities = "SCOPE_match.read")
    void testGetMatchStatistics() throws Exception {
        MatchingService.MatchStatistics stats = new MatchingService.MatchStatistics();
        stats.setTotalMatches(100L);
        stats.setPendingMatches(30L);
        stats.setCompletedMatches(50L);
        stats.setAverageCompatibilityScore(0.75);

        when(matchingService.getMatchStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/matching/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMatches").value(100))
                .andExpect(jsonPath("$.pendingMatches").value(30))
                .andExpect(jsonPath("$.completedMatches").value(50))
                .andExpect(jsonPath("$.averageCompatibilityScore").value(0.75));

        verify(matchingService).getMatchStatistics();
    }

    private MatchDto createTestMatchDto() {
        MatchDto match = new MatchDto();
        match.setId(UUID.randomUUID());
        match.setMatchType("TWO_WAY_CYCLE");
        match.setStatus("PENDING");
        match.setPairIds(Arrays.asList(UUID.randomUUID(), UUID.randomUUID()));
        match.setCompatibilityScore(0.85);
        match.setHospitalId("HOSPITAL_001");
        return match;
    }
}
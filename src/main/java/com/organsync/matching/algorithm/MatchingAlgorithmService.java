package com.organsync.matching.algorithm;

import com.organsync.matching.dto.MatchDto;
import com.organsync.matching.entity.Compatibility;
import com.organsync.matching.entity.MatchStatus;
import com.organsync.matching.entity.MatchType;
import org.jgrapht.Graph;
import org.jgrapht.alg.matching.blossom.v5.KolmogorovWeightedPerfectMatching;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced kidney exchange matching algorithms using JGraphT
 * Implements Edmonds' Blossom Algorithm for maximum weight matching
 */
@Service
public class MatchingAlgorithmService {

    private static final Logger logger = LoggerFactory.getLogger(MatchingAlgorithmService.class);

    /**
     * Find optimal matches using Edmonds' Blossom Algorithm
     * @param compatibilities List of compatibility relationships
     * @return List of optimal matches
     */
    public List<MatchDto> findOptimalMatches(List<Compatibility> compatibilities) {
        logger.info("Starting optimal matching algorithm with {} compatibilities", compatibilities.size());

        // Build compatibility graph
        Graph<UUID, DefaultWeightedEdge> graph = buildCompatibilityGraph(compatibilities);

        // Apply Edmonds' Blossom Algorithm
        KolmogorovWeightedPerfectMatching<UUID, DefaultWeightedEdge> matching =
                new KolmogorovWeightedPerfectMatching<>(graph);

        Set<DefaultWeightedEdge> matchingEdges = matching.getMatching().getEdges();

        List<MatchDto> matches = new ArrayList<>();

        for (DefaultWeightedEdge edge : matchingEdges) {
            UUID source = graph.getEdgeSource(edge);
            UUID target = graph.getEdgeTarget(edge);
            double weight = graph.getEdgeWeight(edge);

            MatchDto match = new MatchDto();
            match.setMatchType(MatchType.TWO_WAY_CYCLE.name());
            match.setStatus(MatchStatus.PENDING.name());
            match.setPairIds(Arrays.asList(source, target));
            match.setCompatibilityScore(weight);

            matches.add(match);
        }

        logger.info("Found {} optimal matches", matches.size());
        return matches;
    }

    /**
     * Find cycles in the compatibility graph
     * @param compatibilities List of compatibility relationships
     * @param maxCycleLength Maximum cycle length to consider
     * @return List of cycle matches
     */
    public List<MatchDto> findCycles(List<Compatibility> compatibilities, int maxCycleLength) {
        logger.info("Finding cycles with max length: {}", maxCycleLength);

        Graph<UUID, DefaultWeightedEdge> graph = buildCompatibilityGraph(compatibilities);
        List<MatchDto> cycles = new ArrayList<>();

        // Find 2-way cycles
        if (maxCycleLength >= 2) {
            cycles.addAll(findTwoWayCycles(graph));
        }

        // Find 3-way cycles
        if (maxCycleLength >= 3) {
            cycles.addAll(findThreeWayCycles(graph));
        }

        logger.info("Found {} cycles", cycles.size());
        return cycles;
    }

    /**
     * Find chains starting from altruistic donors
     * @param compatibilities List of compatibility relationships
     * @param altruisticDonors List of altruistic donor IDs
     * @param maxChainLength Maximum chain length
     * @return List of chain matches
     */
    public List<MatchDto> findChains(List<Compatibility> compatibilities,
                                     List<UUID> altruisticDonors, int maxChainLength) {
        logger.info("Finding chains with max length: {} from {} altruistic donors", maxChainLength, altruisticDonors.size());

        Graph<UUID, DefaultWeightedEdge> graph = buildCompatibilityGraph(compatibilities);
        List<MatchDto> chains = new ArrayList<>();

        for (UUID altruisticDonor : altruisticDonors) {
            List<List<UUID>> foundChains = findChainsFromDonor(graph, altruisticDonor, maxChainLength);

            for (List<UUID> chain : foundChains) {
                MatchDto match = new MatchDto();
                match.setMatchType(MatchType.CHAIN.name());
                match.setStatus(MatchStatus.PENDING.name());
                match.setPairIds(chain);
                match.setCompatibilityScore(calculateChainScore(graph, chain));

                chains.add(match);
            }
        }

        logger.info("Found {} chains", chains.size());
        return chains;
    }

    /**
     * Build compatibility graph from compatibility relationships
     */
    private Graph<UUID, DefaultWeightedEdge> buildCompatibilityGraph(List<Compatibility> compatibilities) {
        Graph<UUID, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        // Add vertices
        Set<UUID> vertices = compatibilities.stream()
                .flatMap(c -> Arrays.stream(new UUID[]{c.getDonorPairId(), c.getRecipientPairId()}))
                .collect(Collectors.toSet());

        vertices.forEach(graph::addVertex);

        // Add edges with weights
        for (Compatibility compatibility : compatibilities) {
            if (compatibility.getBloodTypeCompatible() &&
                    compatibility.getHlaCompatible() &&
                    compatibility.getCrossmatchCompatible()) {

                UUID donor = compatibility.getDonorPairId();
                UUID recipient = compatibility.getRecipientPairId();

                DefaultWeightedEdge edge = graph.addEdge(donor, recipient);
                if (edge != null) {
                    graph.setEdgeWeight(edge, compatibility.getCompatibilityScore());
                }
            }
        }

        return graph;
    }

    /**
     * Find 2-way cycles in the graph
     */
    private List<MatchDto> findTwoWayCycles(Graph<UUID, DefaultWeightedEdge> graph) {
        List<MatchDto> cycles = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();

        for (UUID vertex : graph.vertexSet()) {
            if (!visited.contains(vertex)) {
                for (DefaultWeightedEdge edge : graph.outgoingEdgesOf(vertex)) {
                    UUID neighbor = graph.getEdgeTarget(edge);
                    // Check for reciprocal edge
                    if (graph.containsEdge(neighbor, vertex)) {
                        // Avoid duplicates
                        if (!visited.contains(neighbor) && vertex.compareTo(neighbor) < 0) {
                            double totalScore = graph.getEdgeWeight(edge);
                            DefaultWeightedEdge reverseEdge = graph.getEdge(neighbor, vertex);
                            if (reverseEdge != null) {
                                totalScore += graph.getEdgeWeight(reverseEdge);
                            }
                            MatchDto cycle = new MatchDto();
                            cycle.setMatchType(MatchType.TWO_WAY_CYCLE.name());
                            cycle.setStatus(MatchStatus.PENDING.name());
                            cycle.setPairIds(Arrays.asList(vertex, neighbor));
                            cycle.setCompatibilityScore(totalScore);

                            cycles.add(cycle);
                            visited.add(vertex);
                            visited.add(neighbor);
                        }
                    }
                }
            }
        }

        return cycles;
    }

    /**
     * Find 3-way cycles in the graph
     */
    private List<MatchDto> findThreeWayCycles(Graph<UUID, DefaultWeightedEdge> graph) {
        List<MatchDto> cycles = new ArrayList<>();
        Set<Set<UUID>> visited = new HashSet<>();

        for (UUID v1 : graph.vertexSet()) {
            for (DefaultWeightedEdge e1 : graph.outgoingEdgesOf(v1)) {
                UUID v2 = graph.getEdgeTarget(e1);
                for (DefaultWeightedEdge e2 : graph.outgoingEdgesOf(v2)) {
                    UUID v3 = graph.getEdgeTarget(e2);
                    if (!v3.equals(v1) && graph.containsEdge(v3, v1)) {
                        Set<UUID> cycleSet = Set.of(v1, v2, v3);
                        if (!visited.contains(cycleSet)) {
                            double totalScore = 0.0;
                            DefaultWeightedEdge e12 = graph.getEdge(v1, v2);
                            DefaultWeightedEdge e23 = graph.getEdge(v2, v3);
                            DefaultWeightedEdge e31 = graph.getEdge(v3, v1);
                            if (e12 != null) totalScore += graph.getEdgeWeight(e12);
                            if (e23 != null) totalScore += graph.getEdgeWeight(e23);
                            DefaultWeightedEdge revEdge = graph.getEdge(v3, v1);
                            if (revEdge != null) {
                                totalScore += graph.getEdgeWeight(revEdge);
                            }
                            MatchDto cycle = new MatchDto();
                            cycle.setMatchType(MatchType.THREE_WAY_CYCLE.name());
                            cycle.setStatus(MatchStatus.PENDING.name());
                            cycle.setPairIds(Arrays.asList(v1, v2, v3));
                            cycle.setCompatibilityScore(totalScore);
                            cycles.add(cycle);
                            visited.add(cycleSet);
                        }
                    }
                }
            }
        }

        return cycles;
    }

    /**
     * Find chains starting from a specific donor
     */
    private List<List<UUID>> findChainsFromDonor(Graph<UUID, DefaultWeightedEdge> graph,
                                                 UUID startDonor, int maxLength) {
        List<List<UUID>> chains = new ArrayList<>();
        List<UUID> currentChain = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        visited.add(startDonor);
        findChainsRecursive(graph, startDonor, currentChain, chains, maxLength, visited);
        return chains;
    }

    /**
     * Recursive helper to find chains
     */
    private void findChainsRecursive(Graph<UUID, DefaultWeightedEdge> graph, UUID current,
                                     List<UUID> currentChain, List<List<UUID>> chains,
                                     int maxLength, Set<UUID> visited) {
        currentChain.add(current);
        if (currentChain.size() >= 2) {
            chains.add(new ArrayList<>(currentChain));
        }

        if (currentChain.size() >= maxLength) {
            currentChain.remove(currentChain.size() - 1);
            return;
        }

        for (DefaultWeightedEdge e : graph.outgoingEdgesOf(current)) {
            UUID neighbor = graph.getEdgeTarget(e);
            if (!visited.contains(neighbor)) {
                visited.add(neighbor);
                findChainsRecursive(graph, neighbor, currentChain, chains, maxLength, visited);
                currentChain.remove(currentChain.size() - 1);
                visited.remove(neighbor);
            }
        }
    }

    /**
     * Calculate total compatibility score of a chain
     */
    private double calculateChainScore(Graph<UUID, DefaultWeightedEdge> graph, List<UUID> chain) {
        double score = 0.0;
        for (int i = 0; i < chain.size() - 1; i++) {
            DefaultWeightedEdge e = graph.getEdge(chain.get(i), chain.get(i + 1));
            if (e != null) {
                score += graph.getEdgeWeight(e);
            }
        }
        return score;
    }
}

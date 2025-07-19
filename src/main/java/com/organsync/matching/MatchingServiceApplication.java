package com.organsync.matching;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * OrganSync Matching Service
 * 
 * Advanced kidney exchange matching service implementing sophisticated graph algorithms
 * including Edmonds' Blossom Algorithm, Cycle Detection, and Chain Formation.
 * 
 * Features:
 * - Event-driven architecture with Apache Kafka
 * - JGraphT integration for advanced graph algorithms
 * - Real-time matching capabilities
 * - Production-ready with monitoring and observability
 * - Healthcare compliance with HIPAA standards
 * 
 * @author OrganSync Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableKafka
@EnableAsync
@EnableScheduling
public class MatchingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatchingServiceApplication.class, args);
    }
}
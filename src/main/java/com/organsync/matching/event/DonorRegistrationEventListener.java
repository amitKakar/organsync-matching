package com.organsync.matching.event;

import com.organsync.matching.service.MatchingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Kafka event listener for processing donor registration events
 */
@Component
public class DonorRegistrationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(DonorRegistrationEventListener.class);

    @Autowired
    private MatchingService matchingService;

    /**
     * Listen for donor registration events
     */
    @KafkaListener(topics = "donor.registered", groupId = "matching-service-group")
    public void handleDonorRegistered(@Payload String pairIdString, 
                                     @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                     @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                     @Header(KafkaHeaders.OFFSET) long offset) {
        logger.info("Received donor registration event from topic: {}, partition: {}, offset: {}", 
                   topic, partition, offset);

        try {
            UUID pairId = UUID.fromString(pairIdString);
            logger.info("Processing donor registration for pair ID: {}", pairId);

            // Process the pair registration and find matches
            matchingService.processPairRegistration(pairId);

            logger.info("Successfully processed donor registration for pair ID: {}", pairId);
        } catch (Exception e) {
            logger.error("Error processing donor registration event: {}", pairIdString, e);
            // In a production system, you might want to send this to a dead letter queue
        }
    }

    /**
     * Listen for donor update events
     */
    @KafkaListener(topics = "donor.updated", groupId = "matching-service-group")
    public void handleDonorUpdated(@Payload String pairIdString,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        logger.info("Received donor update event from topic: {}", topic);

        try {
            UUID pairId = UUID.fromString(pairIdString);
            logger.info("Processing donor update for pair ID: {}", pairId);

            // Reprocess the pair to update compatibility and matches
            matchingService.processPairRegistration(pairId);

            logger.info("Successfully processed donor update for pair ID: {}", pairId);
        } catch (Exception e) {
            logger.error("Error processing donor update event: {}", pairIdString, e);
        }
    }

    /**
     * Listen for donor deletion events
     */
    @KafkaListener(topics = "donor.deleted", groupId = "matching-service-group")
    public void handleDonorDeleted(@Payload String pairIdString,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        logger.info("Received donor deletion event from topic: {}", topic);

        try {
            UUID pairId = UUID.fromString(pairIdString);
            logger.info("Processing donor deletion for pair ID: {}", pairId);

            // Remove compatibility relationships and invalidate matches
            // This would be implemented based on business requirements

            logger.info("Successfully processed donor deletion for pair ID: {}", pairId);
        } catch (Exception e) {
            logger.error("Error processing donor deletion event: {}", pairIdString, e);
        }
    }
}
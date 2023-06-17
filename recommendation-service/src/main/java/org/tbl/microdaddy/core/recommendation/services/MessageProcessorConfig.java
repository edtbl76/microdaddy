package org.tbl.microdaddy.core.recommendation.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbl.microdaddy.api.core.recommendation.Recommendation;
import org.tbl.microdaddy.api.core.recommendation.RecommendationService;
import org.tbl.microdaddy.api.event.Event;
import org.tbl.microdaddy.api.exceptions.EventProcessingException;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class MessageProcessorConfig {

    private final RecommendationService recommendationService;

    @Autowired
    public MessageProcessorConfig(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Bean
    public Consumer<Event<Integer, Recommendation>> messageProcessor() {
        return integerRecommendationEvent -> {
            log.info("Process message created at {}...", integerRecommendationEvent.getEventCreatedAt());

            switch (integerRecommendationEvent.getEventType()) {
                case CREATE -> {
                    Recommendation recommendation = integerRecommendationEvent.getData();
                    log.info("Create recommendation with id: {}/{}",
                            recommendation.getProductId(),
                            recommendation.getRecommendationId());
                    recommendationService.createRecommendation(recommendation).block();
                }

                case DELETE -> {
                    int productId = integerRecommendationEvent.getKey();
                    log.info("Delete recommendations for product with id: {}", productId);
                    recommendationService.deleteRecommendations(productId).block();
                }

                default -> {
                    String errorMessage = "Incorrect event type: " + integerRecommendationEvent.getEventType() +
                            ", expected a CREATE or DELETE event";
                    log.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
                }
            }

            log.info("Message processing complete");
        };
    }


}

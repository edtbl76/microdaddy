package org.tbl.microdaddy.core.product.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbl.microdaddy.api.core.product.Product;
import org.tbl.microdaddy.api.core.product.ProductService;
import org.tbl.microdaddy.api.event.Event;
import org.tbl.microdaddy.api.exceptions.EventProcessingException;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class MessageProcessorConfig {

    private final ProductService productService;

    @Autowired
    public MessageProcessorConfig(ProductService productService) {
        this.productService = productService;
    }

    @Bean
    public Consumer<Event<Integer, Product>> messageProcessor() {
        return integerProductEvent -> {
            log.info("Process message created at {}...", integerProductEvent.getEventCreatedAt());

            switch (integerProductEvent.getEventType()) {
                case CREATE -> {
                    Product product = integerProductEvent.getData();
                    log.info("Create product with id: {}", product.getProductId());
                    productService.createProduct(product).block();
                }

                case DELETE -> {
                    int productId = integerProductEvent.getKey();
                    log.info("Delete product with id: {}", productId);
                    productService.deleteProduct(productId).block();
                }

                default -> {
                    String errorMessage = "Incorrect event type: " + integerProductEvent.getEventType() +
                            ", expected a CREATE or DELETE event";
                    log.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
                }
            }

            log.info("Message processing complete");
        };
    }


}

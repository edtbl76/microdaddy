package org.tbl.microdaddy.composite.product;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.tbl.microdaddy.api.composite.product.ProductAggregate;
import org.tbl.microdaddy.api.composite.product.RecommendationSummary;
import org.tbl.microdaddy.api.composite.product.ReviewSummary;
import org.tbl.microdaddy.api.core.product.Product;
import org.tbl.microdaddy.api.core.recommendation.Recommendation;
import org.tbl.microdaddy.api.core.review.Review;
import org.tbl.microdaddy.api.event.Event;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.tbl.microdaddy.api.event.Event.Type.CREATE;
import static org.tbl.microdaddy.api.event.Event.Type.DELETE;
import static org.tbl.microdaddy.composite.product.IsSameEvent.sameEventExceptCreatedAt;
import static reactor.core.publisher.Mono.just;

@Slf4j
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        classes = {TestSecurityConfiguration.class},
        properties = {
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=",
                "spring.main.allow-bean-definition-overriding=true",
                "eureka.client.enabled=false"
        }
)
@Import({TestChannelBinderConfiguration.class})
class MessagingTests {

    @Autowired
    private WebTestClient client;

    @Autowired
    private OutputDestination target;

    @BeforeEach
    void setUp() {
        purgeMessages("products");
        purgeMessages("recommendations");
        purgeMessages("reviews");
    }

    @Test
    void createPartialCompositeProduct() {

        ProductAggregate composite = new ProductAggregate(
                1,
                "name",
                1,
                null,
                null,
                null);
        postAndVerifyProduct(composite, ACCEPTED);

        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewMessages = getMessages("reviews");

        assertEquals(1, productMessages.size());

        Event<Integer, Product> expectedEvent = new Event<>(
                CREATE,
                composite.getProductId(),
                new Product(composite.getProductId(), composite.getName(), composite.getWeight(), null));

        assertThat(productMessages.get(0), is(sameEventExceptCreatedAt(expectedEvent)));

        assertEquals(0, recommendationMessages.size());
        assertEquals(0, reviewMessages.size());

    }

    @Test
    void createCompleteCompositeProduct() {

        ProductAggregate composite = new ProductAggregate(
                1,
                "name",
                1,
                singletonList(new RecommendationSummary(1, "author", 1,"content")),
                singletonList(new ReviewSummary(1, "author", "subject", "content")),
                null);
        postAndVerifyProduct(composite, ACCEPTED);

        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewMessages = getMessages("reviews");

        assertEquals(1, productMessages.size());

        Event<Integer, Product> expectedEvent = new Event<>(
                CREATE,
                composite.getProductId(),
                new Product(composite.getProductId(), composite.getName(), composite.getWeight(), null));
        assertThat(productMessages.get(0), is(sameEventExceptCreatedAt(expectedEvent)));

        // Check recommendation
        assertEquals(1, recommendationMessages.size());
        RecommendationSummary recommendationSummary = composite.getRecommendations().get(0);
        Event<Integer, Recommendation> expectedRecommendationEvent = new Event<>(
                CREATE,
                composite.getProductId(),
                new Recommendation(
                        composite.getProductId(),
                        recommendationSummary.getRecommendationId(),
                        recommendationSummary.getAuthor(),
                        recommendationSummary.getRate(),
                        recommendationSummary.getContent(),
                        null
                ));
        assertThat(recommendationMessages.get(0), is(sameEventExceptCreatedAt(expectedRecommendationEvent)));

        // Check review
        assertEquals(1, reviewMessages.size());
        ReviewSummary reviewSummary = composite.getReviews().get(0);
        Event<Integer, Review> expectedReviewEvent = new Event<>(
                CREATE,
                composite.getProductId(),
                new Review(
                        composite.getProductId(),
                        reviewSummary.getReviewId(),
                        reviewSummary.getAuthor(),
                        reviewSummary.getSubject(),
                        reviewSummary.getContent(),
                        null
                ));
        assertThat(reviewMessages.get(0), is(sameEventExceptCreatedAt(expectedReviewEvent)));
    }

    @Test
    void deleteCompositeProduct() {
        deleteAndVerifyProduct(1, ACCEPTED);

        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewMessages = getMessages("reviews");

        // assert 1 delete product event is queued up
        assertEquals(1, productMessages.size());

        Event<Integer, Product> expectedProductEvent = new Event<>(DELETE, 1, null);
        assertThat(productMessages.get(0), is(sameEventExceptCreatedAt(expectedProductEvent)));

        // again for recommendations
        assertEquals(1, recommendationMessages.size());

        Event<Integer, Product> expectedRecommendationEvent = new Event<>(DELETE, 1, null);
        assertThat(recommendationMessages.get(0), is(sameEventExceptCreatedAt(expectedRecommendationEvent)));

        // again for reviews
        assertEquals(1, reviewMessages.size());

        Event<Integer, Product> expectedReviewEvent = new Event<>(DELETE, 1, null);
        assertThat(reviewMessages.get(0), is(sameEventExceptCreatedAt(expectedReviewEvent)));
    }

    // Helpers
    private void purgeMessages(String bindingName) {
        getMessages(bindingName);
    }

    private List<String> getMessages(String bindingName) {
        List<String> messages = new ArrayList<>();
        boolean moreMessages = true;

        while (moreMessages) {
            Message<byte[]> message = getMessage(bindingName);

            if (message == null) {
                moreMessages = false;
            }
            else {
                messages.add(new String(message.getPayload()));
            }
        }
        return messages;
    }

    private Message<byte[]> getMessage(String bindingName) {
        try {
            return target.receive(0, bindingName);
        } catch (NullPointerException e) {
            log.error("getMessage() caught NullPointerException with binding = {}", bindingName);
            return null;
        }
    }

    private void postAndVerifyProduct(ProductAggregate compositeProduct, HttpStatus expectedStatus) {
        client.post()
                .uri("/product-composite")
                .body(just(compositeProduct), ProductAggregate.class)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }

    private void deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        client.delete()
                .uri("/product-composite/" + productId)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }
}

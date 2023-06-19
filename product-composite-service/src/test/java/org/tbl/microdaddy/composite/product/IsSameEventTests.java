package org.tbl.microdaddy.composite.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tbl.microdaddy.api.core.product.Product;
import org.tbl.microdaddy.api.event.Event;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.fasterxml.jackson.databind.DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.tbl.microdaddy.api.event.Event.Type.CREATE;
import static org.tbl.microdaddy.api.event.Event.Type.DELETE;
import static org.tbl.microdaddy.composite.product.IsSameEvent.sameEventExceptCreatedAt;

class IsSameEventTests {

    ObjectMapper mapper = new ObjectMapper()
//            .findAndRegisterModules()
            .registerModule(new JavaTimeModule())
            .disable(WRITE_DATES_AS_TIMESTAMPS)
            .disable(ADJUST_DATES_TO_CONTEXT_TIME_ZONE);


    @Disabled
    @Test
    void whyIsntThisFuckingWorking() throws IOException {
        ObjectMapper myMapper = new ObjectMapper();
        myMapper
                .registerModule(new JavaTimeModule())
                .disable(WRITE_DATES_AS_TIMESTAMPS)
                .disable(ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        ZonedDateTime rightNow = ZonedDateTime.now(ZoneId.of("UTC"));
        String converted = myMapper.writeValueAsString(rightNow);

        ZonedDateTime restored = myMapper.readValue(converted, ZonedDateTime.class);
        System.out.println("serialized: " + rightNow);
        System.out.println("restored: " + restored);
        assertThat(rightNow, is(restored));
    }


    @Test
    void testEventObjectCompare() throws JsonProcessingException {

        /*
            Event 1 & 2 are same event, but non-concurrent
            Event 3 & 4 are different events
         */
        Event<Integer, Product> event1 = new Event<>(CREATE,
                1, new Product(1, "name", 1, null));

        Event<Integer, Product> event2 = new Event<>(CREATE,
                1, new Product(1, "name", 1, null));

        Event<Integer, Product> event3 = new Event<>(DELETE, 1, null);

        Event<Integer, Product> event4 = new Event<>(CREATE,
                2, new Product(2, "name", 1, null));

        /*
            TODO: I think there is a bug in the JsonSerializer (ZonedDateTimeSerializer).
            Disabled tests for now, will come back to this.
         */
        String event1Json = mapper.writeValueAsString(event1);

        assertThat(event1Json, is(sameEventExceptCreatedAt(event2)));
        assertThat(event1Json, not(sameEventExceptCreatedAt(event3)));
        assertThat(event1Json, not(sameEventExceptCreatedAt(event4)));
    }
}

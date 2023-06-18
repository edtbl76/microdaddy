package org.tbl.microdaddy.composite.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.tbl.microdaddy.api.event.Event;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class IsSameEvent extends TypeSafeMatcher<String> {

    private ObjectMapper mapper = new ObjectMapper();
    private Event expectedEvent;

    private IsSameEvent(Event expectedEvent) {
        this.expectedEvent = expectedEvent;
    }

    @Override
    protected boolean matchesSafely(String eventAsJson) {

        if (expectedEvent == null) return false;

        log.trace("Convert following json string to a map: {}", eventAsJson);
        Map mapEvent = convertJsonStringToMap(eventAsJson);
        mapEvent.remove("eventCreatedAt");

        Map mapExpectedEvent = getMapWithoutCreatedAt(expectedEvent);

        log.trace("Got map: {}", mapEvent);
        log.trace("Comparing to expected map: {}", mapExpectedEvent);
        return mapEvent.equals(mapExpectedEvent);
    }

    @Override
    public void describeTo(Description description) {
        String expectedJson = convertObjectToJsonString(expectedEvent);
        description.appendText("expected to look like " + expectedJson);
    }

    public static Matcher<String> sameEventExceptCreatedAt(Event expectedEvent) {
        return new IsSameEvent(expectedEvent);
    }

    private Map getMapWithoutCreatedAt(Event event) {
        Map mapEvent = convertObjectToMap(event);
        mapEvent.remove("eventCreatedAt");
        return mapEvent;
    }

    private Map convertObjectToMap(Object object) {
        JsonNode node = mapper.convertValue(object, JsonNode.class);
        return mapper.convertValue(node, Map.class);
    }

    private String convertObjectToJsonString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Map<?,?> convertJsonStringToMap(String eventAsJson) {
        try {
            return mapper.readValue(eventAsJson, new TypeReference<HashMap>() { });
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}

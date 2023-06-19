package org.tbl.microdaddy.api.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.key.ZonedDateTimeKeySerializer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static java.time.ZonedDateTime.now;

public class Event <K, T> {

    public enum Type {
        CREATE,
        DELETE
    }
    private final Type eventType;
    private final K key;
    private final T data;

    // TODO, there is a bug w/ this. Solve it later.
    private final ZonedDateTime eventCreatedAt;
//    private final LocalDateTime eventCreatedAt;

    public Event() {
        this.eventType = null;
        this.key = null;
        this.data = null;
        this.eventCreatedAt = null;
    }

    public Event(Type eventType, K key, T data) {
        this.eventType = eventType;
        this.key = key;
        this.data = data;
//        this.eventCreatedAt = now().toLocalDateTime();
        this.eventCreatedAt = now();
    }

    public Type getEventType() {
        return eventType;
    }

    public K getKey() {
        return key;
    }

    public T getData() {
        return data;
    }

//    @JsonSerialize(using = ZonedDateTimeKeySerializer.class)
    @JsonSerialize(using = CustomZonedDateTimeSerializer.class)
    public ZonedDateTime getEventCreatedAt() {
        return eventCreatedAt;
    }

//    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
//    public LocalDateTime getEventCreatedAt() {
//        return eventCreatedAt;
//    }
}

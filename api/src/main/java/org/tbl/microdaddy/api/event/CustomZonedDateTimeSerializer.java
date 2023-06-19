package org.tbl.microdaddy.api.event;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CustomZonedDateTimeSerializer extends StdSerializer<ZonedDateTime> {

    private static DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    public CustomZonedDateTimeSerializer() {
        this(null);
    }

    public CustomZonedDateTimeSerializer(final Class<ZonedDateTime> zonedDateTimeClass) {
        super(zonedDateTimeClass);
    }

    @Override
    public void serialize(ZonedDateTime value, JsonGenerator generator, SerializerProvider provider)
            throws IOException {
        generator.writeString(formatter.format(value));
    }
}

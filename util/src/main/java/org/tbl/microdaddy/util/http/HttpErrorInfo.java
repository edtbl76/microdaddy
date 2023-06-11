package org.tbl.microdaddy.util.http;


import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

public class HttpErrorInfo {

    @Getter
    private final ZonedDateTime timestamp;
    private final HttpStatus httpStatus;

    @Getter
    private final String path;

    @Getter
    private final String message;

    public HttpErrorInfo(HttpStatus httpStatus, String path, String message) {
        this.timestamp = ZonedDateTime.now();
        this.httpStatus = httpStatus;
        this.path = path;
        this.message = message;
    }

    public HttpErrorInfo() {
        this(null, null, null);
    }

    public int getHttpStatus() {
        return httpStatus.value();
    }

    public String getError() {
        return httpStatus.getReasonPhrase();
    }
}

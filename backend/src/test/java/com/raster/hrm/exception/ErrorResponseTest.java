package com.raster.hrm.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorResponseTest {

    @Test
    void shouldCreateErrorResponseWithAllFields() {
        var timestamp = java.time.LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        var response = new ErrorResponse(404, "Not found", timestamp);

        assertEquals(404, response.status());
        assertEquals("Not found", response.message());
        assertEquals(timestamp, response.timestamp());
    }
}

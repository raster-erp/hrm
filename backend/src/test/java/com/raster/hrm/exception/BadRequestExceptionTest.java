package com.raster.hrm.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BadRequestExceptionTest {

    @Test
    void shouldContainMessage() {
        var ex = new BadRequestException("Invalid input");

        assertEquals("Invalid input", ex.getMessage());
    }
}

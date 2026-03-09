package com.raster.hrm.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceNotFoundExceptionTest {

    @Test
    void shouldContainResourceDetails() {
        var ex = new ResourceNotFoundException("Employee", "id", 42L);

        assertEquals("Employee not found with id: '42'", ex.getMessage());
        assertEquals("Employee", ex.getResourceName());
        assertEquals("id", ex.getFieldName());
        assertEquals(42L, ex.getFieldValue());
    }
}

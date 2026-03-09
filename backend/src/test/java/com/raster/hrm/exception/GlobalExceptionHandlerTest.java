package com.raster.hrm.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFoundException_shouldReturn404() {
        var ex = new ResourceNotFoundException("Employee", "id", 1L);

        var response = handler.handleResourceNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Employee not found with id: '1'", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void handleBadRequestException_shouldReturn400() {
        var ex = new BadRequestException("Invalid data");

        var response = handler.handleBadRequestException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Invalid data", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void handleValidationException_shouldReturn400WithFieldErrors() throws NoSuchMethodException {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "name", "must not be blank"));
        bindingResult.addError(new FieldError("testObject", "email", "must be valid"));

        var methodParameter = new MethodParameter(
                this.getClass().getDeclaredMethod("handleValidationException_shouldReturn400WithFieldErrors"), -1);
        var ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        var response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertNotNull(response.getBody().message());
        assert response.getBody().message().contains("name: must not be blank");
        assert response.getBody().message().contains("email: must be valid");
    }

    @Test
    void handleGeneralException_shouldReturn500() {
        var ex = new RuntimeException("Unexpected failure");

        var response = handler.handleGeneralException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals("An unexpected error occurred", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }
}

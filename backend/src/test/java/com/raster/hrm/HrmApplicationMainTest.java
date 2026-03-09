package com.raster.hrm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class HrmApplicationMainTest {

    @Test
    void mainMethodRunsWithoutException() {
        assertDoesNotThrow(() -> HrmApplication.main(new String[]{"--spring.profiles.active=test"}));
    }
}

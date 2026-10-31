package com.mb.modulith;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.modulith.core.ApplicationModules;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationModulesVerificationTest {

    @Test
    void verify_ShouldPass_WhenModulesOnlyDependOnAllowedModules() {
        // Arrange
        ApplicationModules modules = ApplicationModules.of(ModulithApplication.class);

        // Act
        // Assertions
        assertDoesNotThrow((Executable) modules::verify);
        assertTrue(modules.getModuleByName("localstack").isPresent());
    }
}

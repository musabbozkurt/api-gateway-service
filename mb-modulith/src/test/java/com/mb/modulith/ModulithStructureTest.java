package com.mb.modulith;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ModulithStructureTest {

    @Test
    void verifyModulithStructure_ShouldLoadCoreTypes_WhenPresentOnClasspath() {
        // Arrange
        // Act
        // Assertions
        assertDoesNotThrow(() -> Class.forName("com.mb.modulith.ModulithApplication"));
        assertDoesNotThrow(() -> Class.forName("com.mb.modulith.school.api.controller.StudentController"));
        assertDoesNotThrow(() -> Class.forName("com.mb.modulith.clinic.api.controller.PatientController"));
        assertDoesNotThrow(() -> Class.forName("com.mb.modulith.commerce.api.controller.ClientController"));
        assertDoesNotThrow(() -> Class.forName("com.mb.modulith.email.api.controller.EmailController"));
        assertDoesNotThrow(() -> Class.forName("com.mb.modulith.warehouse.api.controller.FoodController"));
        assertDoesNotThrow(() -> Class.forName("com.mb.modulith.playermarket.api.controller.TeamController"));
        assertDoesNotThrow(() -> Class.forName("com.mb.modulith.delivery.api.controller.PersonController"));
    }
}
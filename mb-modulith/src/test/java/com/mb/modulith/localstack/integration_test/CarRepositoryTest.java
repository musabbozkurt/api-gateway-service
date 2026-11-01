package com.mb.modulith.localstack.integration_test;

import com.mb.modulith.localstack.config.LocalStackConfiguration;
import com.mb.modulith.localstack.data.model.Car;
import com.mb.modulith.localstack.data.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
@Import(LocalStackConfiguration.class)
class CarRepositoryTest {

    @Autowired
    private CarRepository carRepository;

    @BeforeEach
    void setUp() {
        carRepository.deleteAll(carRepository.findAll());
    }

    @Test
    void save_ShouldPersistNewElement_WhenElementDoesNotExist() {
        // Arrange
        var carOne = new Car(null, "123456789", "Fiat");

        // Act
        carRepository.save(carOne);

        // Assertions
        List<Car> retrievedCars = carRepository.findAll();
        assertThat(retrievedCars).hasSize(1);
        assertThat(retrievedCars.getFirst()).isEqualTo(carOne);
    }

    @Test
    void save_ShouldUpdateElement_WhenElementAlreadyExists() {
        // Arrange
        var carOne = new Car(null, "123456789", "Fiat");
        carRepository.save(carOne);

        List<Car> retrievedCars = carRepository.findAll();
        assertThat(retrievedCars).hasSize(1);
        assertThat(retrievedCars.getFirst()).isEqualTo(carOne);
        assertThat(retrievedCars.getFirst().getName()).isEqualTo("Fiat");

        // Act
        carOne.setName("Mercedes");
        carRepository.save(carOne);

        // Assertions
        retrievedCars = carRepository.findAll();
        assertThat(retrievedCars).hasSize(1);
        assertThat(retrievedCars.getFirst()).isEqualTo(carOne);
        assertThat(retrievedCars.getFirst().getName()).isEqualTo("Mercedes");
    }

    @Test
    void delete_ShouldRemoveElement_WhenElementExists() {
        // Arrange
        var carOne = new Car(null, "123456789", "Fiat");
        carRepository.save(carOne);
        String id = carOne.getId();
        assertThat(id).isNotNull();

        Optional<Car> retrievedCar = carRepository.findById(id);
        assertThat(retrievedCar)
                .isPresent()
                .contains(carOne);

        // Act
        carRepository.delete(carOne);

        // Assertions
        Optional<Car> carNotRetrieved = carRepository.findById(id);
        assertFalse(carNotRetrieved.isPresent());
    }
}

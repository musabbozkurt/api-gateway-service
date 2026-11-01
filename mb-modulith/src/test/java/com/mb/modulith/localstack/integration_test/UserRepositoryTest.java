package com.mb.modulith.localstack.integration_test;

import com.mb.modulith.localstack.config.LocalStackConfiguration;
import com.mb.modulith.localstack.data.model.User;
import com.mb.modulith.localstack.data.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(LocalStackConfiguration.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(userRepository.findAll());
    }

    @Test
    void save_ShouldPersistNewElement_WhenElementDoesNotExist() {
        // Arrange
        var userOne = new User(null, "TestName", "TestSurname");

        // Act
        userRepository.save(userOne);

        // Assertions
        List<User> retrievedUsers = userRepository.findAll();
        assertThat(retrievedUsers).hasSize(1);
        assertThat(retrievedUsers.getFirst()).isEqualTo(userOne);
    }

    @Test
    void save_ShouldUpdateElement_WhenElementAlreadyExists() {
        // Arrange
        var userOne = new User(null, "TestName", "TestSurname");
        userRepository.save(userOne);

        List<User> retrievedUsers = userRepository.findAll();
        assertThat(retrievedUsers).hasSize(1);
        assertThat(retrievedUsers.getFirst()).isEqualTo(userOne);
        assertThat(retrievedUsers.getFirst().getFirstname()).isEqualTo("TestName");

        // Act
        userOne.setFirstname("Enzo");
        userRepository.save(userOne);

        // Assertions
        retrievedUsers = userRepository.findAll();
        assertThat(retrievedUsers).hasSize(1);
        assertThat(retrievedUsers.getFirst()).isEqualTo(userOne);
        assertThat(retrievedUsers.getFirst().getFirstname()).isEqualTo("Enzo");
    }

    @Test
    void delete_ShouldRemoveElement_WhenElementExists() {
        // Arrange
        var userOne = new User(null, "TestName", "TestSurname");
        userRepository.save(userOne);
        String id = userOne.getId();
        assertThat(id).isNotNull();

        Optional<User> retrievedUser = userRepository.findById(id);
        assertThat(retrievedUser)
                .isPresent()
                .contains(userOne);

        // Act
        userRepository.delete(userOne);

        // Assertions
        Optional<User> userNotRetrieved = userRepository.findById(id);
        assertThat(userNotRetrieved).isNotPresent();
    }
}

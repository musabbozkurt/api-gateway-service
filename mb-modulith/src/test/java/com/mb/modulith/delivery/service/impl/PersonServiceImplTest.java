package com.mb.modulith.delivery.service.impl;

import com.mb.modulith.delivery.base.BaseUnitTest;
import com.mb.modulith.delivery.data.model.Person;
import com.mb.modulith.delivery.data.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonServiceImplTest extends BaseUnitTest {

    @Mock
    private PersonRepository mockPersonRepository;

    private PersonServiceImpl personServiceImplUnderTest;

    @BeforeEach
    void setUp() {
        personServiceImplUnderTest = new PersonServiceImpl(mockPersonRepository);
    }

    @Test
    void save_ShouldReturnPerson_WhenRepositoryPersistsEntity() {
        // Arrange
        Person person = getCustomer();
        when(mockPersonRepository.save(person)).thenReturn(person);

        // Act
        Person result = personServiceImplUnderTest.save(person);

        // Assertions
        assertEquals(person, result);
    }

    @Test
    void findById_ShouldReturnPerson_WhenRepositoryFindsEntity() {
        // Arrange
        Person person = getCustomer();
        when(mockPersonRepository.findById(0L)).thenReturn(Optional.of(person));

        // Act
        Person result = personServiceImplUnderTest.findById(0L);

        // Assertions
        assertEquals(person, result);
    }

    @Test
    void findById_ShouldReturnNull_WhenRepositoryReturnsEmpty() {
        // Arrange
        when(mockPersonRepository.findById(0L)).thenReturn(Optional.empty());

        // Act
        Person result = personServiceImplUnderTest.findById(0L);

        // Assertions
        assertNull(result);
    }

    @Test
    void getAll_ShouldReturnAllPersons_WhenRepositoryHasData() {
        // Arrange
        List<Person> persons = getAllPersons();
        when(mockPersonRepository.findAll()).thenReturn(persons);

        // Act
        List<Person> result = personServiceImplUnderTest.getAll();

        // Assertions
        assertEquals(persons, result);
    }
}

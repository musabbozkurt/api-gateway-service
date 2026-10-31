package com.mb.modulith.delivery.mapper;

import com.mb.modulith.delivery.api.request.ApiPersonRequest;
import com.mb.modulith.delivery.api.response.ApiPersonResponse;
import com.mb.modulith.delivery.base.BaseUnitTest;
import com.mb.modulith.delivery.data.model.Person;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PersonMapperTest extends BaseUnitTest {

    private final PersonMapper personMapper = Mappers.getMapper(PersonMapper.class);

    @Test
    void map_ShouldMapPersonToApiPersonResponse_WhenPersonIsValid() {
        // Arrange
        Person person = getCustomer();

        // Act
        ApiPersonResponse result = personMapper.map(person);

        // Assertions
        assertEquals(person.getId(), result.getId());
        assertEquals(person.getEmail(), result.getEmail());
    }

    @Test
    void map_ShouldMapApiPersonRequestToPerson_WhenRequestIsValid() {
        // Arrange
        ApiPersonRequest apiPersonRequest = getApiPersonRequest();

        // Act
        Person result = personMapper.map(apiPersonRequest);

        // Assertions
        assertEquals(apiPersonRequest.getEmail(), result.getEmail());
        assertEquals(apiPersonRequest.getType(), result.getType());
    }

    @Test
    void map_ShouldMapAllPersons_WhenListIsProvided() {
        // Arrange
        List<Person> persons = getAllPersons();

        // Act
        List<ApiPersonResponse> result = personMapper.map(persons);

        // Assertions
        assertEquals(persons.size(), result.size());
    }

    @Test
    void map_ShouldReturnNull_WhenPersonIsNull() {
        // Act
        ApiPersonResponse result = personMapper.map((Person) null);

        // Assertions
        assertNull(result);
    }

    @Test
    void map_ShouldReturnNull_WhenApiPersonRequestIsNull() {
        // Act
        Person result = personMapper.map((ApiPersonRequest) null);

        // Assertions
        assertNull(result);
    }

    @Test
    void map_ShouldMapAllFields_WhenPersonIsComplete() {
        // Arrange
        Person person = getCustomer();
        person.setId(11L);
        person.setName("Mike");
        person.setRegistrationNumber("123456");

        // Act
        ApiPersonResponse result = personMapper.map(person);

        // Assertions
        assertEquals(person.getId(), result.getId());
        assertEquals(person.getName(), result.getName());
        assertEquals(person.getEmail(), result.getEmail());
        assertEquals(person.getRegistrationNumber(), result.getRegistrationNumber());
        assertEquals(person.getType(), result.getType());
    }
}

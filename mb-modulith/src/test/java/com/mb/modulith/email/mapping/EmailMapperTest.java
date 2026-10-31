package com.mb.modulith.email.mapping;

import com.mb.modulith.email.api.dto.request.ApiEmailRequest;
import com.mb.modulith.email.api.dto.response.ApiEmailResponse;
import com.mb.modulith.email.client.emailresource.dto.DataSetResponseDto;
import com.mb.modulith.email.data.model.Email;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailMapperTest {

    private final EmailMapper emailMapper = Mappers.getMapper(EmailMapper.class);

    @Test
    void map_ShouldMapRequestToEntity_WhenRequestIsValid() {
        // Arrange
        ApiEmailRequest request = new ApiEmailRequest();
        request.setEmailAddress("user@mb.com");
        request.setBlockedDomain(false);

        // Act
        Email result = emailMapper.map(request);

        // Assertions
        assertEquals("user@mb.com", result.getEmailAddress());
    }

    @Test
    void map_ShouldMapEntityToResponse_WhenEntityIsValid() {
        // Arrange
        Email email = new Email();
        email.setEmailAddress("user@mb.se");
        email.setEmailCount(2);
        email.setBlockedDomain(true);

        // Act
        ApiEmailResponse result = emailMapper.map(email);

        // Assertions
        assertEquals("user@mb.se", result.getEmailAddress());
        assertEquals(2, result.getEmailCount());
    }

    @Test
    void map_ShouldMapEmailList_WhenListIsNotEmpty() {
        // Arrange
        Email email = new Email();
        email.setEmailAddress("a@mb.com");
        List<Email> emails = List.of(email);

        // Act
        List<ApiEmailResponse> result = emailMapper.map(emails);

        // Assertions
        assertEquals(1, result.size());
    }

    @Test
    void map_ShouldReturnSameDataSet_WhenDtoIsProvided() {
        // Arrange
        DataSetResponseDto dto = new DataSetResponseDto();

        // Act
        DataSetResponseDto result = emailMapper.map(dto);

        // Assertions
        assertEquals(dto.getEmails(), result.getEmails());
        assertEquals(dto.getResources(), result.getResources());
    }
}

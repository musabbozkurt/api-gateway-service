package com.mb.modulith.delivery.api.controller;

import com.mb.modulith.delivery.api.request.ApiPersonRequest;
import com.mb.modulith.delivery.api.response.ApiPersonResponse;
import com.mb.modulith.delivery.base.BaseUnitTest;
import com.mb.modulith.delivery.data.model.Person;
import com.mb.modulith.delivery.enums.ParticipantType;
import com.mb.modulith.delivery.mapper.PersonMapper;
import com.mb.modulith.delivery.service.PersonService;
import com.mb.modulith.support.CommerceWebMvcTestSecurity;
import com.mb.modulith.support.JacksonTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@CommerceWebMvcTestSecurity
@Import(JacksonTestConfiguration.class)
@WebMvcTest(controllers = PersonController.class)
@ContextConfiguration(classes = PersonController.class)
class PersonControllerTest extends BaseUnitTest {

    @MockitoBean
    private PersonService personService;

    @MockitoBean
    private PersonMapper personMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_ShouldReturnPersonResponse_WhenRequestIsValid() throws Exception {
        // Arrange
        ApiPersonRequest request = new ApiPersonRequest(null, "Jack", "abc@gmail.com", "123456", ParticipantType.CUSTOMER);
        Person entity = getCustomer();
        ApiPersonResponse response = new ApiPersonResponse(3L, "Jack", "abc@gmail.com", "123456", ParticipantType.CUSTOMER);
        when(personMapper.map(any(ApiPersonRequest.class))).thenReturn(entity);
        when(personService.save(any(Person.class))).thenReturn(entity);
        when(personMapper.map(any(Person.class))).thenReturn(response);

        // Act
        ResultActions result = mockMvc.perform(post("/api/person")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Assertions
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("abc@gmail.com"));
    }

    @Test
    void getAllPersons_ShouldReturnList_WhenPersonsExist() throws Exception {
        // Arrange
        when(personService.getAll()).thenReturn(getAllPersons());
        when(personMapper.map(anyList())).thenReturn(List.of(new ApiPersonResponse(1L, "Mike", "abc@gmail.com", "123456", ParticipantType.CUSTOMER)));

        // Act
        ResultActions result = mockMvc.perform(get("/api/person"));

        // Assertions
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Mike"));
    }

    @Test
    void getPersonById_ShouldReturnPerson_WhenPersonExists() throws Exception {
        // Arrange
        Person person = getCustomer();
        person.setId(7L);
        when(personService.findById(7L)).thenReturn(person);

        // Act
        ResultActions result = mockMvc.perform(get("/api/person/{id}", 7L));

        // Assertions
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.email").value("email"));
    }

    @Test
    void getPersonById_ShouldReturn404_WhenPersonMissing() throws Exception {
        // Arrange
        when(personService.findById(404L)).thenReturn(null);

        // Act
        ResultActions result = mockMvc.perform(get("/api/person/{id}", 404L));

        // Assertions
        result.andExpect(status().isNotFound());
    }
}

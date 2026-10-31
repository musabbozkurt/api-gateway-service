package com.mb.modulith.commerce.api.controller;

import com.mb.modulith.commerce.base.BaseUnitTest;
import com.mb.modulith.commerce.service.AuthService;
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
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@CommerceWebMvcTestSecurity
@Import(JacksonTestConfiguration.class)
@WebMvcTest(controllers = AuthController.class)
@ContextConfiguration(classes = AuthController.class)
class AuthControllerTest extends BaseUnitTest {

    @MockitoBean
    private AuthService authService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void authenticateUser_ShouldReturn200_WhenCredentialsAreValid() throws Exception {
        // Arrange
        when(authService.getJwtResponseResponseEntity(getApiClientAuthRequest())).thenReturn(getJwtResponse());

        // Act
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getApiClientAuthRequest())))
                .andExpect(status().isOk());
    }
}

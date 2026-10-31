package com.mb.modulith.playermarket.api.controller;

import com.mb.modulith.playermarket.base.BaseUnitTest;
import com.mb.modulith.playermarket.service.PlayerService;
import com.mb.modulith.playermarket.service.dto.player.ApiPlayerResponse;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(JacksonTestConfiguration.class)
@WebMvcTest(controllers = PlayerController.class)
@ContextConfiguration(classes = PlayerController.class)
class PlayerControllerTest extends BaseUnitTest {

    @MockitoBean
    PlayerService playerService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createPlayer_ShouldReturnBody_WhenServiceReturnsPlayer() throws Exception {
        // Arrange
        ApiPlayerResponse apiPlayerResponse = getApiPlayerResponse();
        when(playerService.createPlayer(any())).thenReturn(apiPlayerResponse);

        // Act
        mockMvc.perform(post("/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getApiPlayerRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(apiPlayerResponse.getName()));
    }

    @Test
    void getPlayers_ShouldReturnList_WhenServiceReturnsPlayers() throws Exception {
        // Arrange
        ApiPlayerResponse apiPlayerResponse = getApiPlayerResponse();
        when(playerService.getPlayers()).thenReturn(Collections.singletonList(apiPlayerResponse));

        // Act
        mockMvc.perform(get("/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(apiPlayerResponse.getName()));
    }
}

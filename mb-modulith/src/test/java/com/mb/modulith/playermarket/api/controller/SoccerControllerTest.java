package com.mb.modulith.playermarket.api.controller;

import com.mb.modulith.playermarket.base.BaseUnitTest;
import com.mb.modulith.playermarket.service.SoccerService;
import com.mb.modulith.playermarket.service.dto.team.ApiTeamResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SoccerController.class)
@ContextConfiguration(classes = SoccerController.class)
class SoccerControllerTest extends BaseUnitTest {

    @MockitoBean
    SoccerService soccerService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getTeamByPlayerId_ShouldReturnTeam_WhenPlayerExists() throws Exception {
        // Arrange
        ApiTeamResponse apiTeamResponse = getApiTeamResponse();
        UUID playerId = UUID.randomUUID();
        when(soccerService.getTeamByPlayerId(any())).thenReturn(apiTeamResponse);

        // Act
        mockMvc.perform(get("/soccer/team-by-player-id/{playerId}", playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(apiTeamResponse.getName()));
    }
}

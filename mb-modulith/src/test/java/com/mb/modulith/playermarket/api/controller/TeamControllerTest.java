package com.mb.modulith.playermarket.api.controller;

import com.mb.modulith.playermarket.base.BaseUnitTest;
import com.mb.modulith.playermarket.service.TeamService;
import com.mb.modulith.playermarket.service.dto.team.ApiTeamResponse;
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
@WebMvcTest(controllers = TeamController.class)
@ContextConfiguration(classes = TeamController.class)
class TeamControllerTest extends BaseUnitTest {

    @MockitoBean
    TeamService teamService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createTeam_ShouldReturnBody_WhenServiceReturnsTeam() throws Exception {
        // Arrange
        ApiTeamResponse apiTeamResponse = getApiTeamResponse();
        when(teamService.createTeam(any())).thenReturn(apiTeamResponse);

        // Act
        mockMvc.perform(post("/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getApiTeamRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(apiTeamResponse.getName()));
    }

    @Test
    void getTeams_ShouldReturnList_WhenServiceReturnsTeams() throws Exception {
        // Arrange
        ApiTeamResponse apiTeamResponse = getApiTeamResponse();
        when(teamService.getTeams()).thenReturn(Collections.singletonList(apiTeamResponse));

        // Act
        mockMvc.perform(get("/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(apiTeamResponse.getName()));
    }
}

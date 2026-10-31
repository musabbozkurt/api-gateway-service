package com.mb.modulith.playermarket.service.mapping;

import com.mb.modulith.playermarket.base.BaseUnitTest;
import com.mb.modulith.playermarket.data.model.TeamInfo;
import com.mb.modulith.playermarket.service.dto.team.ApiTeamRequest;
import com.mb.modulith.playermarket.service.dto.team.ApiTeamResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TeamInfoMapperTest extends BaseUnitTest {

    private final TeamInfoMapper teamInfoMapper = Mappers.getMapper(TeamInfoMapper.class);

    @Test
    void toEntity_ShouldMapRequest_WhenRequestIsValid() {
        // Arrange
        ApiTeamRequest request = getApiTeamRequest();

        // Act
        TeamInfo result = teamInfoMapper.toEntity(request);

        // Assertions
        assertEquals(request.getName(), result.getName());
        assertEquals(request.getEmail(), result.getEmail());
    }

    @Test
    void toDto_ShouldMapEntity_WhenEntityIsValid() {
        // Arrange
        TeamInfo teamInfo = getTeamInfo();

        // Act
        ApiTeamResponse result = teamInfoMapper.toDto(teamInfo);

        // Assertions
        assertEquals(teamInfo.getName(), result.getName());
        assertEquals(teamInfo.getShortName(), result.getShortName());
    }

    @Test
    void toDto_ShouldMapEntityList_WhenListIsNotEmpty() {
        // Arrange
        List<TeamInfo> teams = List.of(getTeamInfo());

        // Act
        List<ApiTeamResponse> result = teamInfoMapper.toDto(teams);

        // Assertions
        assertEquals(1, result.size());
    }

    @Test
    void toEntity_ShouldUpdateExistingTeam_WhenRequestIsValid() {
        // Arrange
        TeamInfo existing = getTeamInfo();
        ApiTeamRequest request = getApiTeamRequest();
        request.setName("Updated FC");
        request.setSquad(Collections.emptyList());

        // Act
        TeamInfo result = teamInfoMapper.toEntity(existing, request);

        // Assertions
        assertEquals("Updated FC", result.getName());
        assertEquals(request.getCountry(), result.getCountry());
    }
}

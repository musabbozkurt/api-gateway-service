package com.mb.modulith.playermarket.service.impl;

import com.mb.modulith.playermarket.base.BaseUnitTest;
import com.mb.modulith.playermarket.data.model.TeamInfo;
import com.mb.modulith.playermarket.data.repository.TeamInfoRepository;
import com.mb.modulith.playermarket.service.dto.team.ApiTeamRequest;
import com.mb.modulith.playermarket.service.dto.team.ApiTeamResponse;
import com.mb.modulith.playermarket.service.mapping.TeamInfoMapper;
import com.mb.modulith.shared.exception.BaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest extends BaseUnitTest {

    @Mock
    private TeamInfoRepository teamInfoRepository;

    @Mock
    private TeamInfoMapper teamInfoMapper;

    @InjectMocks
    private TeamServiceImpl teamService;

    @Test
    void createTeam_ShouldReturnDto_WhenSaved() {
        // Arrange
        ApiTeamRequest request = getApiTeamRequest();
        TeamInfo entity = getTeamInfo();
        ApiTeamResponse response = getApiTeamResponse();
        when(teamInfoMapper.toEntity(request)).thenReturn(entity);
        when(teamInfoRepository.save(entity)).thenReturn(entity);
        when(teamInfoMapper.toDto(entity)).thenReturn(response);

        // Act
        ApiTeamResponse result = teamService.createTeam(request);

        // Assertions
        assertEquals(response, result);
    }

    @Test
    void getTeams_ShouldMapActiveTeams_WhenTeamsExist() {
        // Arrange
        TeamInfo teamInfo = getTeamInfo();
        when(teamInfoRepository.findAllByDeletedIsFalse()).thenReturn(List.of(teamInfo));
        when(teamInfoMapper.toDto(List.of(teamInfo))).thenReturn(List.of(getApiTeamResponse()));

        // Act
        List<ApiTeamResponse> result = teamService.getTeams();

        // Assertions
        assertEquals(1, result.size());
    }

    @Test
    void deleteTeam_ShouldSoftDelete_WhenTeamExists() {
        // Arrange
        UUID id = UUID.randomUUID();
        TeamInfo teamInfo = getTeamInfo();
        when(teamInfoRepository.findById(id)).thenReturn(Optional.of(teamInfo));

        // Act
        teamService.deleteTeam(id);

        // Assertions
        verify(teamInfoRepository).save(teamInfo);
    }

    @Test
    void deleteTeam_ShouldThrow_WhenTeamMissing() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(teamInfoRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        // Assertions
        assertThrows(BaseException.class, () -> teamService.deleteTeam(id));
    }
}

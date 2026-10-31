package com.mb.modulith.playermarket.service.impl;

import com.mb.modulith.playermarket.base.BaseUnitTest;
import com.mb.modulith.playermarket.data.model.PlayerInfo;
import com.mb.modulith.playermarket.data.model.TeamInfo;
import com.mb.modulith.playermarket.data.repository.PlayerInfoRepository;
import com.mb.modulith.playermarket.data.repository.TeamInfoRepository;
import com.mb.modulith.playermarket.service.mapping.TeamInfoMapper;
import com.mb.modulith.shared.exception.BaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SoccerServiceImplTest extends BaseUnitTest {

    @Mock
    private PlayerInfoRepository playerInfoRepository;

    @Mock
    private TeamInfoRepository teamInfoRepository;

    @Mock
    private TeamInfoMapper teamInfoMapper;

    @InjectMocks
    private SoccerServiceImpl soccerService;

    @Test
    void addPlayerToTeam_ShouldLinkPlayer_WhenBothExistAndPlayerHasNoTeam() {
        // Arrange
        UUID teamId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        TeamInfo team = getTeamInfo();
        PlayerInfo player = getPlayerInfo(null);
        when(teamInfoRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(playerInfoRepository.findById(playerId)).thenReturn(Optional.of(player));

        // Act
        soccerService.addPlayerToTeam(teamId, playerId);

        // Assertions
        verify(playerInfoRepository).save(player);
    }

    @Test
    void addPlayerToTeam_ShouldThrow_WhenPlayerAlreadyOnTeam() {
        // Arrange
        UUID teamId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        TeamInfo team = getTeamInfo();
        PlayerInfo player = getPlayerInfo(team);
        when(teamInfoRepository.findById(teamId)).thenReturn(Optional.of(team));
        when(playerInfoRepository.findById(playerId)).thenReturn(Optional.of(player));

        // Act
        // Assertions
        assertThrows(BaseException.class, () -> soccerService.addPlayerToTeam(teamId, playerId));
    }

    @Test
    void addPlayerToTeam_ShouldThrow_WhenTeamMissing() {
        // Arrange
        UUID teamId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        when(teamInfoRepository.findById(teamId)).thenReturn(Optional.empty());

        // Act
        // Assertions
        assertThrows(BaseException.class, () -> soccerService.addPlayerToTeam(teamId, playerId));
    }
}

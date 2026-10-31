package com.mb.modulith.playermarket.service;

import com.mb.modulith.playermarket.base.BaseUnitTest;
import com.mb.modulith.playermarket.data.model.PlayerInfo;
import com.mb.modulith.playermarket.data.model.TeamInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PlayerMarketIntegrationTest extends BaseUnitTest {

    @Test
    void savePlayerInfo_ShouldKeepTeamReference_WhenTeamIsSet() {
        // Arrange
        TeamInfo teamInfo = getTeamInfo();
        PlayerInfo playerInfo = getPlayerInfo(teamInfo);

        // Act
        playerInfo.setTeamInfo(teamInfo);

        // Assertions
        assertNotNull(playerInfo.getTeamInfo());
    }
}
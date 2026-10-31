package com.mb.modulith.playermarket.service.mapping;

import com.mb.modulith.playermarket.base.BaseUnitTest;
import com.mb.modulith.playermarket.data.model.PlayerInfo;
import com.mb.modulith.playermarket.service.dto.player.ApiPlayerRequest;
import com.mb.modulith.playermarket.service.dto.player.ApiPlayerResponse;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerInfoMapperTest extends BaseUnitTest {

    private final PlayerInfoMapper playerInfoMapper = new PlayerInfoMapperImpl();

    @Test
    void toEntity_ShouldMapFields_WhenUpdatingExistingEntity() {
        // Arrange
        ApiPlayerRequest apiPlayerRequest = getApiPlayerRequest();

        // Act
        PlayerInfo result = playerInfoMapper.toEntity(new PlayerInfo(), apiPlayerRequest);

        // Assertions
        assertEquals(apiPlayerRequest.getName(), result.getName());
    }

    @Test
    void toDto_ShouldMapFields_WhenEntityIsValid() {
        // Arrange
        PlayerInfo playerInfo = getPlayerInfo(null);

        // Act
        ApiPlayerResponse result = playerInfoMapper.toDto(playerInfo);

        // Assertions
        assertEquals(playerInfo.getName(), result.getName());
    }

    @Test
    void toEntity_ShouldCreateEntity_WhenRequestIsValid() {
        // Arrange
        ApiPlayerRequest apiPlayerRequest = getApiPlayerRequest();

        // Act
        PlayerInfo result = playerInfoMapper.toEntity(apiPlayerRequest);

        // Assertions
        assertEquals(apiPlayerRequest.getName(), result.getName());
    }

    @Test
    void toDto_ShouldMapList_WhenEntitiesProvided() {
        // Arrange
        PlayerInfo playerInfo = getPlayerInfo(null);

        // Act
        List<ApiPlayerResponse> result = playerInfoMapper.toDto(Collections.singletonList(playerInfo));

        // Assertions
        assertEquals(1, result.size());
    }
}

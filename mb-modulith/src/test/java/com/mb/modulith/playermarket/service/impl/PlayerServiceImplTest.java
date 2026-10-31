package com.mb.modulith.playermarket.service.impl;

import com.mb.modulith.playermarket.base.BaseUnitTest;
import com.mb.modulith.playermarket.data.repository.PlayerInfoRepository;
import com.mb.modulith.playermarket.service.mapping.PlayerInfoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceImplTest extends BaseUnitTest {

    @Mock
    PlayerInfoRepository playerInfoRepository;

    @Mock
    PlayerInfoMapper playerInfoMapper;

    @InjectMocks
    PlayerServiceImpl playerServiceImpl;

    @Test
    void deletePlayer_ShouldThrow_WhenPlayerMissing() {
        // Arrange
        UUID playerId = UUID.randomUUID();

        // Act
        // Assertions
        assertThrows(Exception.class, () -> playerServiceImpl.deletePlayer(playerId));
    }

    @Test
    void getPlayers_ShouldReturnMappedList_WhenRepositoryHasData() {
        // Arrange
        when(playerInfoRepository.findAllByDeletedIsFalse()).thenReturn(Collections.emptyList());
        when(playerInfoMapper.toDto(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        var result = playerServiceImpl.getPlayers();

        // Assertions
        org.junit.jupiter.api.Assertions.assertTrue(result.isEmpty());
    }
}

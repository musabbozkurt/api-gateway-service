package com.mb.modulith.playermarket.service.impl;

import com.mb.modulith.playermarket.data.repository.TeamInfoRepository;
import com.mb.modulith.playermarket.service.dto.fee.ApiFeeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FeeServiceImplTest {

    @Mock
    private TeamInfoRepository teamInfoRepository;

    @InjectMocks
    private FeeServiceImpl feeServiceImpl;

    @BeforeEach
    void setUp() {
        // Arrange
    }

    @Test
    void calculateFeeByTeamId_ShouldThrow_WhenTeamNotFound() {
        // Arrange
        UUID teamId = UUID.randomUUID();
        ApiFeeRequest request = new ApiFeeRequest(0, new BigDecimal(0), 0);

        // Act
        // Assertions
        verify(teamInfoRepository, never()).findById(teamId);
        assertThrows(Exception.class, () -> feeServiceImpl.calculateFeeByTeamId(teamId, request));
    }
}

package com.mb.modulith.playermarket.service;

import com.mb.modulith.playermarket.service.dto.team.ApiTeamResponse;

import java.util.UUID;

public interface SoccerService {

    void addPlayerToTeam(UUID teamId, UUID playerInfoId);

    ApiTeamResponse getTeamByPlayerId(UUID playerId);
}

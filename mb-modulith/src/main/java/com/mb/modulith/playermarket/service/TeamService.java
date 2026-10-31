package com.mb.modulith.playermarket.service;

import com.mb.modulith.playermarket.service.dto.team.ApiTeamRequest;
import com.mb.modulith.playermarket.service.dto.team.ApiTeamResponse;

import java.util.List;
import java.util.UUID;

public interface TeamService {

    ApiTeamResponse createTeam(ApiTeamRequest apiTeamRequest);

    List<ApiTeamResponse> getTeams();

    void deleteTeam(UUID teamInfoId);

    ApiTeamResponse updateTeam(UUID teamInfoId, ApiTeamRequest apiTeamRequest);
}

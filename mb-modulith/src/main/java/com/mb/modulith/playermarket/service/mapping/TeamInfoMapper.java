package com.mb.modulith.playermarket.service.mapping;

import com.mb.modulith.playermarket.data.model.TeamInfo;
import com.mb.modulith.playermarket.service.dto.team.ApiTeamRequest;
import com.mb.modulith.playermarket.service.dto.team.ApiTeamResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamInfoMapper extends EntityMapper<ApiTeamResponse, TeamInfo> {

    ApiTeamResponse toDto(TeamInfo teamInfo);

    TeamInfo toEntity(ApiTeamRequest apiTeamRequest);

    List<ApiTeamResponse> toDto(List<TeamInfo> entityList);

    default TeamInfo toEntity(TeamInfo teamInfo, ApiTeamRequest apiTeamRequest) {
        teamInfo.setName(apiTeamRequest.getName());
        teamInfo.setCountry(apiTeamRequest.getCountry());
        teamInfo.setClubColors(apiTeamRequest.getClubColors());
        teamInfo.setEmail(apiTeamRequest.getEmail());
        teamInfo.setSquad(apiTeamRequest.getSquad());
        teamInfo.setShortName(apiTeamRequest.getShortName());
        return teamInfo;
    }

}

package com.mb.modulith.playermarket.service.impl;

import com.mb.modulith.playermarket.data.model.TeamInfo;
import com.mb.modulith.playermarket.data.repository.TeamInfoRepository;
import com.mb.modulith.playermarket.exception.MarketErrorCode;
import com.mb.modulith.playermarket.service.TeamService;
import com.mb.modulith.playermarket.service.dto.team.ApiTeamRequest;
import com.mb.modulith.playermarket.service.dto.team.ApiTeamResponse;
import com.mb.modulith.playermarket.service.mapping.TeamInfoMapper;
import com.mb.modulith.shared.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamInfoRepository teamInfoRepository;
    private final TeamInfoMapper teamInfoMapper;

    @Override
    public ApiTeamResponse createTeam(ApiTeamRequest apiTeamRequest) {
        return teamInfoMapper.toDto(teamInfoRepository.save(teamInfoMapper.toEntity(apiTeamRequest)));
    }

    @Override
    public List<ApiTeamResponse> getTeams() {
        return teamInfoMapper.toDto(teamInfoRepository.findAllByDeletedIsFalse());
    }

    @Override
    public void deleteTeam(UUID teamInfoId) {
        Optional<TeamInfo> optionalTeamInfo = teamInfoRepository.findById(teamInfoId);
        if (optionalTeamInfo.isPresent()) {
            TeamInfo teamInfo = optionalTeamInfo.get();
            teamInfo.setDeleted(true);
            teamInfoRepository.save(teamInfo);
        } else {
            throw new BaseException(MarketErrorCode.TEAM_INFO_NOT_FOUND);
        }
    }

    @Override
    public ApiTeamResponse updateTeam(UUID teamInfoId, ApiTeamRequest apiTeamRequest) {
        Optional<TeamInfo> optionalTeamInfo = teamInfoRepository.findById(teamInfoId);
        if (optionalTeamInfo.isPresent()) {
            TeamInfo teamInfo = teamInfoMapper.toEntity(optionalTeamInfo.get(), apiTeamRequest);
            return teamInfoMapper.toDto(teamInfoRepository.save(teamInfo));
        } else {
            throw new BaseException(MarketErrorCode.TEAM_INFO_NOT_FOUND);
        }
    }
}

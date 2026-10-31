package com.mb.modulith.playermarket.service.impl;

import com.mb.modulith.playermarket.data.model.PlayerInfo;
import com.mb.modulith.playermarket.data.model.TeamInfo;
import com.mb.modulith.playermarket.data.repository.PlayerInfoRepository;
import com.mb.modulith.playermarket.data.repository.TeamInfoRepository;
import com.mb.modulith.playermarket.exception.MarketErrorCode;
import com.mb.modulith.playermarket.service.SoccerService;
import com.mb.modulith.playermarket.service.dto.team.ApiTeamResponse;
import com.mb.modulith.playermarket.service.mapping.TeamInfoMapper;
import com.mb.modulith.shared.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SoccerServiceImpl implements SoccerService {

    private final PlayerInfoRepository playerInfoRepository;
    private final TeamInfoRepository teamInfoRepository;
    private final TeamInfoMapper teamInfoMapper;

    @Override
    public void addPlayerToTeam(UUID teamId, UUID playerInfoId) {
        Optional<TeamInfo> optionalTeamInfo = teamInfoRepository.findById(teamId);

        if (optionalTeamInfo.isPresent()) {
            TeamInfo teamInfo = optionalTeamInfo.get();

            Optional<PlayerInfo> optionalPlayerInfo = playerInfoRepository.findById(playerInfoId);

            if (optionalPlayerInfo.isPresent()) {
                PlayerInfo playerInfo = optionalPlayerInfo.get();
                if (Objects.isNull(playerInfo.getTeamInfo())) {
                    playerInfo.setTeamInfo(teamInfo);
                    playerInfoRepository.save(playerInfo);
                } else {
                    throw new BaseException(MarketErrorCode.PLAYER_ALREADY_HAS_TEAM_EXCEPTION);
                }
            } else {
                throw new BaseException(MarketErrorCode.PLAYER_INFO_NOT_FOUND);
            }
        } else {
            throw new BaseException(MarketErrorCode.TEAM_INFO_NOT_FOUND);
        }
    }

    @Override
    public ApiTeamResponse getTeamByPlayerId(UUID playerId) {
        Optional<PlayerInfo> optionalPlayerInfo = playerInfoRepository.findById(playerId);
        if (optionalPlayerInfo.isPresent()) {
            TeamInfo teamInfo = optionalPlayerInfo.get().getTeamInfo();
            if (Objects.nonNull(teamInfo)) {
                teamInfo.setSquad(null);
                return teamInfoMapper.toDto(teamInfo);
            } else {
                throw new BaseException(MarketErrorCode.TEAM_INFO_NOT_FOUND);
            }
        } else {
            throw new BaseException(MarketErrorCode.PLAYER_INFO_NOT_FOUND);
        }
    }

}

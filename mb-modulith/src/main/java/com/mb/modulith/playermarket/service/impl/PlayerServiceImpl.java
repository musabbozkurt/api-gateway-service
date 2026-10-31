package com.mb.modulith.playermarket.service.impl;

import com.mb.modulith.playermarket.data.model.PlayerInfo;
import com.mb.modulith.playermarket.data.repository.PlayerInfoRepository;
import com.mb.modulith.playermarket.exception.MarketErrorCode;
import com.mb.modulith.playermarket.service.PlayerService;
import com.mb.modulith.playermarket.service.dto.player.ApiPlayerRequest;
import com.mb.modulith.playermarket.service.dto.player.ApiPlayerResponse;
import com.mb.modulith.playermarket.service.mapping.PlayerInfoMapper;
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
public class PlayerServiceImpl implements PlayerService {

    private final PlayerInfoRepository playerInfoRepository;
    private final PlayerInfoMapper playerInfoMapper;

    @Override
    public ApiPlayerResponse createPlayer(ApiPlayerRequest apiPlayerRequest) {
        return playerInfoMapper.toDto(playerInfoRepository.save(playerInfoMapper.toEntity(apiPlayerRequest)));
    }

    @Override
    public List<ApiPlayerResponse> getPlayers() {
        return playerInfoMapper.toDto(playerInfoRepository.findAllByDeletedIsFalse());
    }

    @Override
    public void deletePlayer(UUID playerInfoId) {
        Optional<PlayerInfo> optionalPlayerInfo = playerInfoRepository.findById(playerInfoId);
        if (optionalPlayerInfo.isPresent()) {
            PlayerInfo playerInfo = optionalPlayerInfo.get();
            playerInfo.setDeleted(true);
            playerInfoRepository.save(playerInfo);
        } else {
            throw new BaseException(MarketErrorCode.PLAYER_INFO_NOT_FOUND);
        }
    }

    @Override
    public ApiPlayerResponse updatePlayer(UUID playerInfoId, ApiPlayerRequest apiPlayerRequest) {
        Optional<PlayerInfo> optionalPlayerInfo = playerInfoRepository.findById(playerInfoId);
        if (optionalPlayerInfo.isPresent()) {
            PlayerInfo playerInfo = playerInfoMapper.toEntity(optionalPlayerInfo.get(), apiPlayerRequest);
            return playerInfoMapper.toDto(playerInfoRepository.save(playerInfo));
        } else {
            throw new BaseException(MarketErrorCode.PLAYER_INFO_NOT_FOUND);
        }
    }
}

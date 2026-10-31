package com.mb.modulith.playermarket.service;

import com.mb.modulith.playermarket.service.dto.player.ApiPlayerRequest;
import com.mb.modulith.playermarket.service.dto.player.ApiPlayerResponse;

import java.util.List;
import java.util.UUID;

public interface PlayerService {

    ApiPlayerResponse createPlayer(ApiPlayerRequest apiPlayerRequest);

    List<ApiPlayerResponse> getPlayers();

    void deletePlayer(UUID playerInfoId);

    ApiPlayerResponse updatePlayer(UUID playerInfoId, ApiPlayerRequest apiPlayerRequest);
}

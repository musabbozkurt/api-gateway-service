package com.mb.modulith.playermarket.service.mapping;

import com.mb.modulith.playermarket.data.model.PlayerInfo;
import com.mb.modulith.playermarket.service.dto.player.ApiPlayerRequest;
import com.mb.modulith.playermarket.service.dto.player.ApiPlayerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlayerInfoMapper extends EntityMapper<ApiPlayerResponse, PlayerInfo> {

    ApiPlayerResponse toDto(PlayerInfo playerInfo);

    PlayerInfo toEntity(ApiPlayerRequest apiPlayerRequest);

    List<ApiPlayerResponse> toDto(List<PlayerInfo> entityList);

    default PlayerInfo toEntity(PlayerInfo playerInfo, ApiPlayerRequest apiPlayerRequest) {
        playerInfo.setName(apiPlayerRequest.getName());
        playerInfo.setSurname(apiPlayerRequest.getSurname());
        playerInfo.setContractUntil(apiPlayerRequest.getContractUntil());
        playerInfo.setDateOfBirth(apiPlayerRequest.getDateOfBirth());
        playerInfo.setJerseyNumber(apiPlayerRequest.getJerseyNumber());
        playerInfo.setMarketValue(apiPlayerRequest.getMarketValue());
        playerInfo.setMarketValueCurrency(apiPlayerRequest.getMarketValueCurrency());
        playerInfo.setNationality(apiPlayerRequest.getNationality());
        playerInfo.setPosition(apiPlayerRequest.getPosition());
        return playerInfo;
    }

}

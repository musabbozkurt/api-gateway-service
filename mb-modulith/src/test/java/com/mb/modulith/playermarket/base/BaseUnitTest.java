package com.mb.modulith.playermarket.base;

import com.mb.modulith.playermarket.data.model.PlayerInfo;
import com.mb.modulith.playermarket.data.model.TeamInfo;
import com.mb.modulith.playermarket.enums.PositionType;
import com.mb.modulith.playermarket.service.dto.player.ApiPlayerRequest;
import com.mb.modulith.playermarket.service.dto.player.ApiPlayerResponse;
import com.mb.modulith.playermarket.service.dto.team.ApiTeamRequest;
import com.mb.modulith.playermarket.service.dto.team.ApiTeamResponse;
import com.neovisionaries.i18n.CountryCode;
import com.neovisionaries.i18n.CurrencyCode;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public abstract class BaseUnitTest {

    public TeamInfo getTeamInfo() {
        TeamInfo buildTeamInfo = new TeamInfo();
        buildTeamInfo.setName("Barcelona");
        buildTeamInfo.setCountry(CountryCode.ES);
        buildTeamInfo.setCurrencyCode(CurrencyCode.USD);
        buildTeamInfo.setClubColors("red-blue");
        buildTeamInfo.setEmail("barcelona@barca.com");
        buildTeamInfo.setShortName("Barca");
        buildTeamInfo.setUpdatedDateTime(OffsetDateTime.now());
        return buildTeamInfo;
    }

    public PlayerInfo getPlayerInfo(TeamInfo teamInfo) {
        PlayerInfo buildPlayerInfo = new PlayerInfo();
        buildPlayerInfo.setName("Leo");
        buildPlayerInfo.setSurname("Messi");
        buildPlayerInfo.setContractUntil("10-06-2023");
        buildPlayerInfo.setDateOfBirth("12-03-1988");
        buildPlayerInfo.setJerseyNumber(10);
        buildPlayerInfo.setMarketValueCurrency(CurrencyCode.USD);
        buildPlayerInfo.setMarketValue(BigDecimal.valueOf(2133443.32));
        buildPlayerInfo.setPosition(PositionType.CF);
        buildPlayerInfo.setTeamInfo(teamInfo);
        buildPlayerInfo.setUpdatedDateTime(OffsetDateTime.now());
        return buildPlayerInfo;
    }

    public ApiTeamRequest getApiTeamRequest() {
        ApiTeamRequest apiTeamRequest = new ApiTeamRequest();
        apiTeamRequest.setName("Barcelona");
        apiTeamRequest.setCountry(CountryCode.ES);
        apiTeamRequest.setCurrencyCode(CurrencyCode.USD);
        apiTeamRequest.setClubColors("red-blue");
        apiTeamRequest.setEmail("barcelona@barca.com");
        apiTeamRequest.setShortName("Barca");
        return apiTeamRequest;
    }

    public ApiPlayerRequest getApiPlayerRequest() {
        ApiPlayerRequest apiPlayerRequest = new ApiPlayerRequest();
        apiPlayerRequest.setName("Leo");
        apiPlayerRequest.setSurname("Messi");
        apiPlayerRequest.setContractUntil("10-06-2023");
        apiPlayerRequest.setDateOfBirth("12-03-1988");
        apiPlayerRequest.setJerseyNumber(10);
        apiPlayerRequest.setMarketValueCurrency(CurrencyCode.USD);
        apiPlayerRequest.setMarketValue(BigDecimal.valueOf(2133443.32));
        apiPlayerRequest.setPosition(PositionType.CF);
        return apiPlayerRequest;
    }

    public ApiPlayerResponse getApiPlayerResponse() {
        ApiPlayerResponse apiPlayerResponse = new ApiPlayerResponse();
        apiPlayerResponse.setName("Leo");
        apiPlayerResponse.setSurname("Messi");
        apiPlayerResponse.setContractUntil("10-06-2023");
        apiPlayerResponse.setDateOfBirth("12-03-1988");
        apiPlayerResponse.setJerseyNumber(10);
        apiPlayerResponse.setMarketValueCurrency(CurrencyCode.USD);
        apiPlayerResponse.setMarketValue(BigDecimal.valueOf(2133443.32));
        apiPlayerResponse.setPosition(PositionType.CF);
        return apiPlayerResponse;
    }

    public ApiTeamResponse getApiTeamResponse() {
        ApiTeamResponse apiTeamResponse = new ApiTeamResponse();
        apiTeamResponse.setName("Barcelona");
        apiTeamResponse.setCountry(CountryCode.ES);
        apiTeamResponse.setCurrencyCode(CurrencyCode.USD);
        apiTeamResponse.setClubColors("red-blue");
        apiTeamResponse.setEmail("barcelona@barca.com");
        apiTeamResponse.setShortName("Barca");
        return apiTeamResponse;
    }

}

package com.mb.modulith.playermarket.service.impl;

import com.mb.modulith.playermarket.data.model.TeamInfo;
import com.mb.modulith.playermarket.data.repository.TeamInfoRepository;
import com.mb.modulith.playermarket.exception.MarketErrorCode;
import com.mb.modulith.playermarket.service.FeeService;
import com.mb.modulith.playermarket.service.dto.fee.ApiFeeRequest;
import com.mb.modulith.playermarket.service.dto.fee.ApiFeeResponse;
import com.mb.modulith.shared.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service("playermarketFeeServiceImpl")
public class FeeServiceImpl implements FeeService {

    private static final int CONSTANT_NUMBER = 100000;
    private static final int TEAM_COMMISSION_LIMIT = 10;
    private final TeamInfoRepository teamInfoRepository;

    @Override
    public ApiFeeResponse calculateFeeByTeamId(UUID teamId, ApiFeeRequest apiFeeRequest) {
        if (apiFeeRequest.getTeamCommission().compareTo(BigDecimal.valueOf(TEAM_COMMISSION_LIMIT)) > 0) {
            throw new BaseException(MarketErrorCode.TEAM_COMMISSION_LIMIT_EXCEEDED);
        }
        Optional<TeamInfo> optionalTeamInfo = teamInfoRepository.findById(teamId);
        if (optionalTeamInfo.isPresent()) {
            int feeCalculation = apiFeeRequest.getMonthOfExpirence() * CONSTANT_NUMBER;

            BigDecimal transferFee = BigDecimal.valueOf(feeCalculation / apiFeeRequest.getAge());

            BigDecimal teamCommission = transferFee.multiply(apiFeeRequest.getTeamCommission())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.CEILING);

            return ApiFeeResponse.builder().contractFee(transferFee.add(teamCommission)).contractFeeCurrency(optionalTeamInfo.get().getCurrencyCode()).build();
        } else {
            throw new BaseException(MarketErrorCode.TEAM_INFO_NOT_FOUND);
        }

    }

}

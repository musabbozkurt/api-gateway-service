package com.mb.modulith.playermarket.api.controller;

import com.mb.modulith.playermarket.base.BaseUnitTest;
import com.mb.modulith.playermarket.service.FeeService;
import com.mb.modulith.playermarket.service.dto.fee.ApiFeeResponse;
import com.neovisionaries.i18n.CurrencyCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FeeController.class)
@ContextConfiguration(classes = FeeController.class)
class FeeControllerTest extends BaseUnitTest {

    @MockitoBean
    FeeService feeService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void calculateFeeByTeamId_ShouldReturnFee_WhenServiceResponds() throws Exception {
        // Arrange
        ApiFeeResponse fee = ApiFeeResponse.builder()
                .contractFee(BigDecimal.ZERO)
                .contractFeeCurrency(CurrencyCode.USD)
                .build();
        UUID teamId = UUID.randomUUID();
        when(feeService.calculateFeeByTeamId(any(), any())).thenReturn(fee);

        // Act
        mockMvc.perform(get("/fee/{teamId}", teamId)
                        .param("age", "0")
                        .param("teamCommission", "0")
                        .param("monthOfExpirence", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractFee").value(0));
    }
}

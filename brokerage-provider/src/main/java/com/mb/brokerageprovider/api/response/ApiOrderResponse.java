package com.mb.brokerageprovider.api.response;

import com.mb.brokerageprovider.enums.OrderStatus;
import com.mb.brokerageprovider.enums.OrderType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiOrderResponse {

    @Schema(description = "Order id")
    private Long id;

    @Schema(description = "Order creation date")
    private OffsetDateTime createdDateTime;

    @Schema(description = "Order modified date")
    private OffsetDateTime modifiedDateTime;

    private ApiUserResponse user;

    @Schema(description = "Order status")
    private OrderStatus status;

    @Schema(description = "Order type")
    private OrderType type;

    private String productCode;

    private Long quantity;
}

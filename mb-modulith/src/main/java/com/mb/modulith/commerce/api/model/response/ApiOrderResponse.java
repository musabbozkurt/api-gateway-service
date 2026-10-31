package com.mb.modulith.commerce.api.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class ApiOrderResponse {
    private UUID id;
    private Date createdDateTime;
    private boolean deleted;
    private UUID orderId;
}

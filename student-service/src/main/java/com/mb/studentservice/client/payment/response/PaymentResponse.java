package com.mb.studentservice.client.payment.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class PaymentResponse {

    private long id;
    private String name;
}
package com.mb.paymentservice.mapper;

import com.mb.paymentservice.api.request.PaymentRequest;
import com.mb.paymentservice.api.response.PaymentResponse;
import com.mb.paymentservice.data.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentResponse map(Payment payment);

    @Mapping(target = "id", ignore = true)
    Payment map(PaymentRequest paymentRequest);

    List<PaymentResponse> map(List<Payment> payments);

}

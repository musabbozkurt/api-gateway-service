package com.mb.paymentservice.service;

import com.mb.paymentservice.data.entity.Payment;

import java.util.List;

public interface PaymentService {

    Payment getPaymentById(Long id);

    Payment createPayment(Payment payment);

    List<Payment> getPaymentList();

}

package com.mb.paymentservice.api.controller;

import com.mb.paymentservice.api.request.PaymentRequest;
import com.mb.paymentservice.api.response.PaymentResponse;
import com.mb.paymentservice.mapper.PaymentMapper;
import com.mb.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @PostMapping
    @RolesAllowed("user")
    public PaymentResponse createPayment(@RequestBody PaymentRequest paymentRequest) {
        return paymentMapper.map(paymentService.createPayment(paymentMapper.map(paymentRequest)));
    }

    @GetMapping("/{id}")
    @RolesAllowed("user")
    public PaymentResponse getPaymentById(@PathVariable Long id) {
        return paymentMapper.map(paymentService.getPaymentById(id));
    }

    @GetMapping
    @RolesAllowed("user")
    public List<PaymentResponse> getPaymentList() {
        return paymentMapper.map(paymentService.getPaymentList());
    }

}

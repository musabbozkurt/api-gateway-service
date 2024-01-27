package com.mb.paymentservice.api.controller;

import com.mb.paymentservice.api.request.PaymentRequest;
import com.mb.paymentservice.api.response.PaymentResponse;
import com.mb.paymentservice.exception.ErrorCode;
import com.mb.paymentservice.exception.PaymentServiceException;
import com.mb.paymentservice.mapper.PaymentMapper;
import com.mb.paymentservice.service.PaymentService;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @PostMapping
    @PreAuthorize("hasRole('client_user')")
    public PaymentResponse createPayment(@RequestBody PaymentRequest paymentRequest) {
        return paymentMapper.map(paymentService.createPayment(paymentMapper.map(paymentRequest)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('client_admin')")
    public PaymentResponse getPaymentById(@PathVariable Long id) {
        return paymentMapper.map(paymentService.getPaymentById(id));
    }

    @GetMapping
    @RolesAllowed("user")
    public List<PaymentResponse> getPaymentList() {
        return paymentMapper.map(paymentService.getPaymentList());
    }

    @GetMapping("/error")
    public List<PaymentResponse> getErrorResponse() {
        throw new PaymentServiceException(ErrorCode.UNKNOWN_ERROR);
    }

}

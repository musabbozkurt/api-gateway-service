package com.mb.studentservice.service.impl;

import com.mb.studentservice.client.keycloak.KeycloakClient;
import com.mb.studentservice.client.payment.PaymentClient;
import com.mb.studentservice.client.payment.response.PaymentResponse;
import com.mb.studentservice.data.entity.Student;
import com.mb.studentservice.data.repository.StudentRepository;
import com.mb.studentservice.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final PaymentClient paymentClient;
    private final KeycloakClient keycloakClient;

    @Override
    public Student saveStudent(Student student) {
        return studentRepository.save(student);
    }

    @Override
    public List<PaymentResponse> getPayments() {
        List<PaymentResponse> paymentClientPayments = paymentClient.getPayments();
        log.info("paymentClientPayments: {}", paymentClientPayments.toString());

        List<PaymentResponse> keycloakClientPayments = keycloakClient.getPayments();
        log.info("keycloakClientPayments: {}", keycloakClientPayments.toString());

        paymentClientPayments.addAll(keycloakClientPayments);

        return paymentClientPayments;
    }

}
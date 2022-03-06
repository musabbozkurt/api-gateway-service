package com.mb.studentservice.service;

import com.mb.studentservice.client.payment.response.PaymentResponse;
import com.mb.studentservice.data.entity.Student;

import java.util.List;

public interface StudentService {

    Student saveStudent(Student student);

    List<PaymentResponse> getPayments();

}
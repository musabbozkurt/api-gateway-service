package com.mb.studentservice.api.controller;

import com.mb.studentservice.api.response.Student;
import com.mb.studentservice.client.payment.response.PaymentResponse;
import com.mb.studentservice.enums.EventType;
import com.mb.studentservice.queue.producer.StudentEventProducer;
import com.mb.studentservice.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/students")
public class StudentController {

    private final StudentEventProducer studentEventProducer;
    private final StudentService studentService;

    private final List<Student> students = new ArrayList<>();

    {
        students.add(new Student(1, "Student-Student1", "ADMIN2", "student.student1@test.com"));
        students.add(new Student(2, "Student-Student2", "SUPERVISOR2", "student.student2@test.com"));
        students.add(new Student(3, "Student-Student3", "USER2", "student.student3@test.com"));
        students.add(new Student(4, "Student-Student4", "USER3", "student.student4@test.com"));
    }

    @GetMapping(value = "/")
    public List<Student> getStudents() {
        return students;
    }

    @GetMapping(value = "/{id}")
    public Student getStudentById(@PathVariable(value = "id") int id) {
        return students.stream()
                .filter(x -> x.getId() == (id))
                .toList()
                .getFirst();
    }

    @GetMapping(value = "/roles/{role}")
    public List<Student> getStudentByRole(@PathVariable(value = "role") String role) {
        return students.stream()
                .filter(x -> x.getRole().equalsIgnoreCase(role))
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/events")
    public void publishStudentEvent() {
        studentEventProducer.publishEvent("Publish Student Event with EventType STUDENT_EVENT", EventType.STUDENT_EVENT);
    }

    @GetMapping(value = "/payments")
    public List<PaymentResponse> getPayments() {
        return studentService.getPayments();
    }

}
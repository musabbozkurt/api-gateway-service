package com.mb.studentservice.api.controller;

import com.mb.studentservice.api.response.Student;
import com.mb.studentservice.client.payment.response.PaymentResponse;
import com.mb.studentservice.enums.EventType;
import com.mb.studentservice.queue.producer.StudentEventProducer;
import com.mb.studentservice.service.StudentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/students")
@Api(value = "Student Rest Controller")
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

    @RequestMapping(value = "/")
    @ApiOperation(value = "Get Students", response = Iterable.class, tags = "getStudents")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success|OK"),
            @ApiResponse(code = 401, message = "Not Authorized!"),
            @ApiResponse(code = 403, message = "Forbidden!"),
            @ApiResponse(code = 404, message = "Not Found!")})
    public List<Student> getStudents() {
        return students;
    }

    @RequestMapping(value = "/{id}")
    @ApiOperation(value = "Get Student by Student Id ", response = Student.class, tags = "getStudentById")
    public Student getStudentById(@PathVariable(value = "id") int id) {
        return students.stream()
                .filter(x -> x.getId() == (id))
                .collect(Collectors.toList())
                .get(0);
    }

    @RequestMapping(value = "/role/{role}")
    @ApiOperation(value = "Get Student by role ", response = Student.class, tags = "getStudentByRole")
    public List<Student> getStudentByRole(@PathVariable(value = "role") String role) {
        return students.stream()
                .filter(x -> x.getRole().equalsIgnoreCase(role))
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/events")
    @ApiOperation(value = "Publish Student Event", response = Student.class, tags = "publishStudentEvent")
    public void publishStudentEvent() {
        studentEventProducer.publishEvent("Publish Student Event with EventType STUDENT_EVENT", EventType.STUDENT_EVENT);
    }

    @RequestMapping(value = "/payments")
    @ApiOperation(value = "Get Payments", response = PaymentResponse.class, tags = "getPayments")
    public List<PaymentResponse> getPayments() {
        return studentService.getPayments();
    }

}
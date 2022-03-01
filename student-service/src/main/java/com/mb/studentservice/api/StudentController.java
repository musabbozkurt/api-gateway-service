package com.mb.studentservice.api;

import com.mb.studentservice.api.response.Student;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Api(value = "Student Rest Controller")
@RequestMapping("/students")
@RestController
public class StudentController {

    List<Student> students = new ArrayList<>();

    {
        students.add(new Student(1, "Student-Student1", "ADMIN2", "student.student1@test.com"));
        students.add(new Student(2, "Student-Student2", "SUPERVISOR2", "student.student2@test.com"));
        students.add(new Student(3, "Student-Student3", "USER2", "student.student3@test.com"));
        students.add(new Student(4, "Student-Student4", "USER3", "student.student4@test.com"));
    }

    @ApiOperation(value = "Get Students", response = Iterable.class, tags = "getStudents")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success|OK"),
            @ApiResponse(code = 401, message = "Not Authorized!"),
            @ApiResponse(code = 403, message = "Forbidden!"),
            @ApiResponse(code = 404, message = "Not Found!")})

    @RequestMapping(value = "/")
    public List<Student> getStudents() {
        return students;
    }

    @ApiOperation(value = "Get Student by Student Id ", response = Student.class, tags = "getStudentById")
    @RequestMapping(value = "/{id}")
    public Student getStudentById(@PathVariable(value = "id") int id) {
        return students.stream()
                .filter(x -> x.getId() == (id))
                .collect(Collectors.toList())
                .get(0);
    }

    @ApiOperation(value = "Get Student by role ", response = Student.class, tags = "getStudentByRole")
    @RequestMapping(value = "/role/{role}")
    public List<Student> getStudentByRole(@PathVariable(value = "role") String role) {
        return students.stream()
                .filter(x -> x.getRole().equalsIgnoreCase(role))
                .collect(Collectors.toList());
    }

}
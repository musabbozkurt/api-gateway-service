package com.mb.modulith.school.service;

import com.mb.modulith.school.api.filter.ApiStudentFilter;
import com.mb.modulith.school.data.model.Course;
import com.mb.modulith.school.data.model.Student;

import java.util.List;
import java.util.Set;

public interface StudentService {

    Student addStudent(Student student);

    Student updateStudent(Long studentId, Student student);

    List<Student> getStudents(ApiStudentFilter apiStudentFilter);

    void removeStudent(Long studentId);

    void registerCourse(Long studentId, Set<Course> courses);

    Set<Student> getStudentsByCourseName(String courseName);

}

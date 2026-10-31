package com.mb.modulith.school.service.impl;

import com.mb.modulith.school.api.filter.ApiStudentFilter;
import com.mb.modulith.school.config.LimitsConfigProperties;
import com.mb.modulith.school.data.model.Course;
import com.mb.modulith.school.data.model.Student;
import com.mb.modulith.school.data.repository.StudentRepository;
import com.mb.modulith.school.exception.SchoolRegistrationException;
import com.mb.modulith.school.service.CourseService;
import com.mb.modulith.school.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;
    private final CourseService courseService;
    private final LimitsConfigProperties configProperties;

    public Student addStudent(Student student) {
        return studentRepository.save(student);
    }

    @Override
    public Student updateStudent(Long studentId, Student student) {
        Optional<Student> optionalStudent = studentRepository.findById(studentId);
        if (optionalStudent.isEmpty()) {
            throw new SchoolRegistrationException("Student is not found. Invalid studentId : " + studentId);
        }
        Student studentFromDb = optionalStudent.get();
        student.setStudentId(studentId);
        student.setCreatedDateTime(studentFromDb.getCreatedDateTime());
        student.setModifiedDateTime(OffsetDateTime.now());
        return studentRepository.save(student);
    }

    @Override
    public List<Student> getStudents(ApiStudentFilter apiStudentFilter) {
        return StreamSupport.stream(studentRepository.findAll(apiStudentFilter.toPredicate()).spliterator(), false).toList();
    }

    public void removeStudent(Long studentId) {
        Optional<Student> student = studentRepository.findById(studentId);
        if (student.isEmpty()) {
            throw new SchoolRegistrationException("Student is not found. Invalid studentId : " + studentId);
        }
        studentRepository.delete(student.get());
    }

    public void registerCourse(Long studentId, Set<Course> courses) {
        Optional<Student> studentOptional = studentRepository.findById(studentId);
        if (studentOptional.isEmpty()) {
            throw new SchoolRegistrationException("Student is not found. Invalid studentId : " + studentId);
        }
        Student student = studentOptional.get();
        if (student.getCourses().size() > configProperties.getMaximumNumberOfCourses()) {
            throw new SchoolRegistrationException("A student can have " + configProperties.getMaximumNumberOfCourses() + " number of courses. courseId : " + studentId);
        }
        courses.addAll(student.getCourses());
        student.setCourses(courses);
        studentRepository.save(student);
    }

    public Set<Student> getStudentsByCourseName(String courseName) {
        Optional<Course> course = courseService.getCourseByCourseName(courseName);
        if (course.isEmpty()) {
            throw new SchoolRegistrationException("Course is empty. Invalid courseName : " + courseName);
        }
        Comparator<Student> studentByName = Comparator.comparing(Student::getStudentName);
        TreeSet<Student> sortedStudents = new TreeSet<>(studentByName);

        Set<Student> students = course.get().getStudents();
        students.forEach(student -> student.setCourses(null));
        sortedStudents.addAll(students);
        return sortedStudents;
    }

}

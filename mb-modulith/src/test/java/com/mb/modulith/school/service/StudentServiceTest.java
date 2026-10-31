package com.mb.modulith.school.service;

import com.mb.modulith.school.base.BaseUnitTest;
import com.mb.modulith.school.config.LimitsConfigProperties;
import com.mb.modulith.school.data.model.Course;
import com.mb.modulith.school.data.model.Student;
import com.mb.modulith.school.data.repository.StudentRepository;
import com.mb.modulith.school.exception.SchoolRegistrationException;
import com.mb.modulith.school.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest extends BaseUnitTest {

    private StudentService studentService;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseService courseService;

    @Mock
    private LimitsConfigProperties configProperties;

    @BeforeEach
    void setUp() {
        studentService = new StudentServiceImpl(studentRepository, courseService, configProperties);
    }

    @Test
    void addStudent_ShouldReturnStudent_WhenStudentIsSaved() {
        // Arrange
        Student student = getStudent();
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        // Act
        Student result = studentService.addStudent(student);

        // Assertions
        assertEquals(2, result.getStudentId().longValue());
    }

    @Test
    void removeStudent_ShouldDeleteStudent_WhenStudentExists() {
        // Arrange
        Student student = getStudent();
        Long studentId = student.getStudentId();
        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(student));

        // Act
        studentService.removeStudent(studentId);

        // Assertions
        verify(studentRepository, times(1)).delete(student);
    }

    @Test
    void removeStudent_ShouldThrowException_WhenStudentNotFound() {
        // Arrange
        Long studentId = getStudent().getStudentId();
        when(studentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        SchoolRegistrationException exception = assertThrows(SchoolRegistrationException.class, () -> studentService.removeStudent(studentId));

        // Assertions
        assertEquals(SchoolRegistrationException.class, exception.getClass());
    }

    @Test
    void registerCourse_ShouldLoadStudent_WhenStudentExists() {
        // Arrange
        Long studentId = getStudent().getStudentId();
        Student source = getStudent();
        source.setStudentId(studentId);
        source.setCourses(Collections.emptySet());
        when(studentRepository.findById(anyLong())).thenReturn(Optional.of(source));
        when(configProperties.getMaximumNumberOfCourses()).thenReturn(5);
        Set<Course> courses = Collections.emptySet();

        // Act
        studentService.registerCourse(studentId, courses);

        // Assertions
        verify(studentRepository, times(1)).findById(studentId);
    }

    @Test
    void registerCourse_ShouldThrowException_WhenStudentNotFound() {
        // Arrange
        Long courseId = getCourse().getCourseId();
        when(studentRepository.findById(anyLong())).thenReturn(Optional.empty());
        Set<Course> courses = Collections.emptySet();

        // Act
        SchoolRegistrationException exception = assertThrows(SchoolRegistrationException.class, () -> studentService.registerCourse(courseId, courses));

        // Assertions
        assertEquals(SchoolRegistrationException.class, exception.getClass());
    }

    @Test
    void getStudentsByCourseName_ShouldReturnStudents_WhenCourseExists() {
        // Arrange
        String courseName = getCourse().getCourseName();
        Course course = getCourse();
        Set<Student> studentSet = new HashSet<>();
        Set<Course> courseSet = new HashSet<>();
        courseSet.add(course);
        Student student = getStudent();
        studentSet.add(student);
        student.setCourses(courseSet);
        course.setStudents(studentSet);
        when(courseService.getCourseByCourseName(anyString())).thenReturn(Optional.of(course));

        // Act
        Set<Student> students = studentService.getStudentsByCourseName(courseName);

        // Assertions
        assertNotNull(students);
        assertEquals(1, students.size());
    }

    @Test
    void getStudentsByCourseName_ShouldThrowException_WhenCourseNotFound() {
        // Arrange
        String courseName = getCourse().getCourseName();
        when(courseService.getCourseByCourseName(anyString())).thenReturn(Optional.empty());

        // Act
        SchoolRegistrationException exception = assertThrows(SchoolRegistrationException.class, () -> studentService.getStudentsByCourseName(courseName));

        // Assertions
        assertEquals(SchoolRegistrationException.class, exception.getClass());
    }
}

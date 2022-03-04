package com.mb.studentservice.service.impl;

import com.mb.studentservice.data.entity.Student;
import com.mb.studentservice.data.repository.StudentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class StudentServiceImplTest {

    @InjectMocks
    private StudentServiceImpl studentService;

    @Mock
    private StudentRepository studentRepository;

    @Captor
    private ArgumentCaptor<Student> captor;

    @Test
    public void shouldCapture() {
        Student student = Student.builder().id(1).name("James").build();
        studentService.saveStudent(student);

        //verify(mock).doSomething(argument.capture());
        verify(studentRepository).save(captor.capture());

        assertEquals("James", captor.getValue().getName());
        assertEquals(1, captor.getValue().getId());
    }

    @Test
    public void shouldCaptureMultipleTimes() {
        Student student = Student.builder().id(1).name("James").build();
        Student student2 = Student.builder().id(2).name("Steph").build();
        Student student3 = Student.builder().id(3).name("Michael").build();

        studentService.saveStudent(student);
        studentService.saveStudent(student2);
        studentService.saveStudent(student3);

        verify(studentRepository, Mockito.times(3)).save(captor.capture());

        List<Student> studentList = captor.getAllValues();

        assertEquals("James", studentList.get(0).getName());
        assertEquals("Steph", studentList.get(1).getName());
        assertEquals("Michael", studentList.get(2).getName());
    }

    @Test
    public void shouldCaptureManually() {
        ArgumentCaptor<Student> argumentCaptor = ArgumentCaptor.forClass(Student.class);

        Student student = Student.builder().id(1).name("James").build();
        studentService.saveStudent(student);

        verify(studentRepository).save(argumentCaptor.capture());
        Student captured = argumentCaptor.getValue();

        assertEquals("James", captured.getName());
    }

}
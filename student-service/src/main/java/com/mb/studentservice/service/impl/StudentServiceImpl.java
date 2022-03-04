package com.mb.studentservice.service.impl;

import com.mb.studentservice.data.entity.Student;
import com.mb.studentservice.data.repository.StudentRepository;
import com.mb.studentservice.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;

    @Override
    public Student saveStudent(Student student) {
        return studentRepository.save(student);
    }

}
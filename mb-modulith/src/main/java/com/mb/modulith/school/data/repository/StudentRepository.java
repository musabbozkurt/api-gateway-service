package com.mb.modulith.school.data.repository;

import com.mb.modulith.school.data.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface StudentRepository extends JpaRepository<Student, Long>, QuerydslPredicateExecutor<Student> {

}

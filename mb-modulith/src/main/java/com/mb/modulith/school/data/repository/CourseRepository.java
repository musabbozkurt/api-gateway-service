package com.mb.modulith.school.data.repository;

import com.mb.modulith.school.data.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long>, QuerydslPredicateExecutor<Course> {

    Optional<Course> findCourseByCourseName(String courseName);

}

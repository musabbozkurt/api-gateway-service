package com.mb.modulith.school.service;

import com.mb.modulith.school.api.filter.ApiCourseFilter;
import com.mb.modulith.school.data.model.Course;
import com.mb.modulith.school.data.model.Student;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CourseService {
    Course addCourse(Course course);

    Course updateCourse(Long courseId, Course course);

    List<Course> getCourses(ApiCourseFilter apiCourseFilter);

    void removeCourse(Long courseId);

    Course registerStudentToCourse(Long courseId, Set<Student> students);

    Optional<Course> getCourseByCourseName(String courseName);
}

package com.mb.modulith.school.service.impl;

import com.mb.modulith.school.api.filter.ApiCourseFilter;
import com.mb.modulith.school.config.LimitsConfigProperties;
import com.mb.modulith.school.data.model.Course;
import com.mb.modulith.school.data.model.Student;
import com.mb.modulith.school.data.repository.CourseRepository;
import com.mb.modulith.school.exception.SchoolRegistrationException;
import com.mb.modulith.school.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final LimitsConfigProperties configProperties;

    public Course addCourse(Course course) {
        return courseRepository.save(course);
    }

    @Override
    public Course updateCourse(Long courseId, Course course) {
        Optional<Course> optionalCourse = courseRepository.findById(courseId);
        if (optionalCourse.isEmpty()) {
            throw new SchoolRegistrationException("Course is not found. Invalid courseId : " + courseId);
        }
        Course courseFromDb = optionalCourse.get();
        course.setCourseId(courseId);
        course.setCreatedDateTime(courseFromDb.getCreatedDateTime());
        course.setModifiedDateTime(OffsetDateTime.now());
        return courseRepository.save(course);
    }

    @Override
    public List<Course> getCourses(ApiCourseFilter apiCourseFilter) {
        return StreamSupport.stream(courseRepository.findAll(apiCourseFilter.toPredicate()).spliterator(), false).toList();
    }

    public void removeCourse(Long courseId) {
        Optional<Course> course = courseRepository.findById(courseId);
        if (course.isEmpty()) {
            throw new SchoolRegistrationException("Course is not found. Invalid courseId : " + courseId);
        }
        courseRepository.delete(course.get());
    }

    public Course registerStudentToCourse(Long courseId, Set<Student> students) {
        Optional<Course> courseOptional = courseRepository.findById(courseId);
        if (courseOptional.isEmpty()) {
            throw new SchoolRegistrationException("Course is not found. Invalid courseId : " + courseId);
        }
        Course course = courseOptional.get();
        if (course.getStudents().size() > configProperties.getMaximumNumberOfStudents()) {
            throw new SchoolRegistrationException("Single Course can have " + configProperties.getMaximumNumberOfStudents() + " number of students. courseId : " + courseId);
        }
        students.addAll(course.getStudents());
        course.setStudents(students);
        return courseRepository.save(course);
    }

    public Optional<Course> getCourseByCourseName(String courseName) {
        return courseRepository.findCourseByCourseName(courseName);
    }

}

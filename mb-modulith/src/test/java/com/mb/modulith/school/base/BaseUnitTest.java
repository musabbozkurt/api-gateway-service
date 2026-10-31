package com.mb.modulith.school.base;

import com.mb.modulith.school.api.request.ApiCourseRequest;
import com.mb.modulith.school.api.request.ApiStudentRequest;
import com.mb.modulith.school.data.model.Course;
import com.mb.modulith.school.data.model.Student;

public abstract class BaseUnitTest {

    public Course getCourse() {
        Course course = new Course();
        course.setCourseId(2L);
        course.setCourseName("JAVA");
        course.setCourseDetails("JAVA_COURSE_DETAIL");
        course.setCourseDuration("5 Weeks");
        return course;
    }

    public ApiCourseRequest getApiCourse() {
        ApiCourseRequest apiCourseRequest = new ApiCourseRequest();
        apiCourseRequest.setCourseName("JAVA");
        apiCourseRequest.setCourseDetails("JAVA_COURSE_DETAIL");
        apiCourseRequest.setCourseDuration("5 Weeks");
        return apiCourseRequest;
    }

    public Student getStudent() {
        Student student = new Student();
        student.setStudentId(2L);
        student.setStudentName("JOHN PAZZO");
        student.setAddress("STUDENT_ADDRESS_4");
        student.setMobileNumber("90123456787");
        return student;
    }

    public ApiStudentRequest getApiStudent() {
        ApiStudentRequest apiStudentRequest = new ApiStudentRequest();
        apiStudentRequest.setStudentName("JOHN PAZZO");
        apiStudentRequest.setAddress("STUDENT_ADDRESS_4");
        apiStudentRequest.setMobileNumber("90123456787");
        return apiStudentRequest;
    }

}

package com.mb.modulith.school.api.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApiCourse {

    private String courseId;

    private String courseName;

    private String courseDetails;

    private String courseDuration;
}

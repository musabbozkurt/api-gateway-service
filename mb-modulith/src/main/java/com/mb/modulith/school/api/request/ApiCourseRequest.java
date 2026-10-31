package com.mb.modulith.school.api.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApiCourseRequest {

    private String courseName;

    private String courseDetails;

    private String courseDuration;
}

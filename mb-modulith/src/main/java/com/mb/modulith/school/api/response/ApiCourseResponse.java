package com.mb.modulith.school.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ApiCourseResponse {

    private Long courseId;

    private String courseName;

    private String courseDetails;

    private String courseDuration;

    private Set<ApiStudent> students;

}

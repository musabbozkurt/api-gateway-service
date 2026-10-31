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
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ApiStudentResponse {

    private Long studentId;

    private String studentName;

    private String mobileNumber;

    private String address;

    private Set<ApiCourse> courses;
}

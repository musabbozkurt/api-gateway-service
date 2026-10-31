package com.mb.modulith.school.api.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApiStudentRequest {

    private String studentName;

    private String mobileNumber;

    private String address;

    private Long courseId;

}

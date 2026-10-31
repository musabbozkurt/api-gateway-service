package com.mb.modulith.school.api.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApiStudent {

    private String studentId;

    private String studentName;

    private String mobileNumber;

    private String address;

    private Long courseId;

}

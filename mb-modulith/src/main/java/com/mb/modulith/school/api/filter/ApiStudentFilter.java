package com.mb.modulith.school.api.filter;

import com.mb.modulith.school.data.model.QStudent;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

@Getter
@Setter
public class ApiStudentFilter implements Filter {

    private Long studentId;
    private String studentName;
    private String mobileNumber;
    private String address;
    private Long courseId;

    @Override
    public Predicate toPredicate() {
        QStudent qStudent = QStudent.student;

        BooleanExpression predicate = qStudent.deleted.isFalse();

        if (Objects.nonNull(studentId)) {
            predicate = predicate.and(qStudent.studentId.eq(studentId));
        }

        if (StringUtils.isNotBlank(studentName)) {
            predicate = predicate.and(qStudent.studentName.eq(studentName));
        }

        if (StringUtils.isNotBlank(mobileNumber)) {
            predicate = predicate.and(qStudent.mobileNumber.eq(mobileNumber));
        }

        if (StringUtils.isNotBlank(address)) {
            predicate = predicate.and(qStudent.address.eq(address));
        }

        if (Objects.nonNull(courseId)) {
            predicate = predicate.and(qStudent.courses.any().courseId.eq(courseId));
        }

        return predicate;
    }

}

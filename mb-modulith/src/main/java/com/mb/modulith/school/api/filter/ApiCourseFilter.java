package com.mb.modulith.school.api.filter;

import com.mb.modulith.school.data.model.QCourse;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

@Getter
@Setter
public class ApiCourseFilter implements Filter {

    private String studentName;
    private String courseName;
    private Long courseId;

    @Override
    public Predicate toPredicate() {
        QCourse qCourse = QCourse.course;

        BooleanExpression predicate = qCourse.deleted.isFalse();

        if (StringUtils.isNotBlank(studentName)) {
            predicate = predicate.and(qCourse.students.any().studentName.eq(studentName));
        }

        if (StringUtils.isNotBlank(courseName)) {
            predicate = predicate.and(qCourse.courseName.eq(courseName));
        }

        if (Objects.nonNull(courseId)) {
            predicate = predicate.and(qCourse.courseId.eq(courseId));
        }

        return predicate;
    }

}

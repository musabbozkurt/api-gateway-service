package com.mb.modulith.school.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

@Entity
@Getter
@Setter
@SQLRestriction("deleted = false")
@Table(name = "course", schema = "school")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE school.course SET deleted=true WHERE course_id=?")
public class Course extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id", unique = true, nullable = false, length = 50)
    private Long courseId;

    @NotEmpty(message = "Please provide a courseName")
    private String courseName;

    private String courseDetails;

    private String courseDuration;

    @JsonIgnore
    // CASCADE.ALL removed to avoid deleting students when a course is deleted
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "student_course",
            schema = "school",
            joinColumns = {
                    @JoinColumn(name = "course_id", referencedColumnName = "course_id", nullable = false)
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
            }
    )
    private Set<Student> students;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((courseDetails == null) ? 0 : courseDetails.hashCode());
        result = prime * result + ((courseDuration == null) ? 0 : courseDuration.hashCode());
        result = prime * result + ((courseId == null) ? 0 : courseId.hashCode());
        result = prime * result + ((courseName == null) ? 0 : courseName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Course other = (Course) obj;
        if (courseDetails == null) {
            if (other.courseDetails != null)
                return false;
        } else if (!courseDetails.equals(other.courseDetails))
            return false;
        if (courseDuration == null) {
            if (other.courseDuration != null)
                return false;
        } else if (!courseDuration.equals(other.courseDuration))
            return false;
        if (courseId == null) {
            if (other.courseId != null)
                return false;
        } else if (!courseId.equals(other.courseId))
            return false;
        if (courseName == null) {
            return other.courseName == null;
        } else return courseName.equals(other.courseName);
    }

    @Override
    public String toString() {
        return "Course [courseId=" + courseId +
                ", courseName=" + courseName +
                ", courseDetails=" + courseDetails +
                ", courseDuration=" + courseDuration +
                "]";
    }

}

package com.mb.studentservice.data.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    private int id;
    private String name;
    private String role;
    private String email;
}

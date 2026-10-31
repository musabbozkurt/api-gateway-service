package com.mb.modulith.clinic.data.entity;

import com.mb.modulith.clinic.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted = false")
@Table(name = "appointments", schema = "clinics")
@SQLDelete(sql = "UPDATE clinics.appointments SET deleted=true WHERE id=?")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    private OffsetDateTime endDate;

    private BigDecimal fee;

    @OneToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    private BigDecimal refundedFee;

    private OffsetDateTime startDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.ACTIVE;

    private boolean deleted;

    @Transient
    private Integer doctorId;

    @Transient
    private Integer patientId;


}

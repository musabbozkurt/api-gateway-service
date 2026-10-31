package com.mb.modulith.commerce.data.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("CLIENT")
@EqualsAndHashCode(callSuper = false)
@Table(name = "client", schema = "commerce")
@EntityListeners(AuditingEntityListener.class)
public class Client extends BaseEntity {
    private String firstName;

    private String lastName;

    private String telephone;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "client", cascade = {CascadeType.PERSIST})
    private List<Order> orders;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", schema = "commerce",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "client", cascade = {CascadeType.PERSIST})
    private List<Address> addresses;
}

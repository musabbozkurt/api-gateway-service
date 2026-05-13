package com.mb.stockexchangeservice.data.repository;

import com.mb.stockexchangeservice.data.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Set<Role> findAllByDefaultRoleIsTrue();
}

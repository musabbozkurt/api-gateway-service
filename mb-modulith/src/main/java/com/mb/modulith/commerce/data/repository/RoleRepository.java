package com.mb.modulith.commerce.data.repository;

import com.mb.modulith.commerce.data.model.Role;
import com.mb.modulith.commerce.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Set<Role> findByNameInAndDeletedIsFalse(List<UserRole> names);

}

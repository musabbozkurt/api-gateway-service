package com.mb.modulith.delivery.data.repository;

import com.mb.modulith.delivery.data.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {

}

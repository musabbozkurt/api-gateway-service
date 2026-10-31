package com.mb.modulith.delivery.service;

import com.mb.modulith.delivery.data.model.Person;

import java.util.List;

public interface PersonService {

    List<Person> getAll();

    Person save(Person person);

    Person findById(Long id);
}

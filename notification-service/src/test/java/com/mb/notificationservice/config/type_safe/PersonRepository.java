package com.mb.notificationservice.config.type_safe;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

public interface PersonRepository extends ListCrudRepository<Person, Long>, ListPagingAndSortingRepository<Person, Long> {
}

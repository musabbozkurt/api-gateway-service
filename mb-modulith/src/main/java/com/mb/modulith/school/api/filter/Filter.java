package com.mb.modulith.school.api.filter;

import com.querydsl.core.types.Predicate;

public interface Filter {

    Predicate toPredicate();

}

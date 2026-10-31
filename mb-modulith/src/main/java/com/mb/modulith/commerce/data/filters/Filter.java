package com.mb.modulith.commerce.data.filters;

import com.querydsl.core.types.Predicate;

public interface Filter {

    Predicate toPredicate();
}

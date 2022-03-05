package com.mb.paymentservice.mapper;

import java.util.List;

public interface EntityMapper<D, E, D2> {

    E toEntity(D dto);

    E clientDtoToEntity(D2 dto);

    D toDto(E entity);

    D clientDtoToApiDto(D2 d2);

    List<E> toEntity(List<D> dtoList);

    List<D> toDto(List<E> entityList);
}

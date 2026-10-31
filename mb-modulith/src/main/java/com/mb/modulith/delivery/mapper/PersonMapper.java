package com.mb.modulith.delivery.mapper;

import com.mb.modulith.delivery.api.request.ApiPersonRequest;
import com.mb.modulith.delivery.api.response.ApiPersonResponse;
import com.mb.modulith.delivery.data.model.Person;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PersonMapper {

    ApiPersonResponse map(Person person);

    Person map(ApiPersonRequest apiPersonRequest);

    List<ApiPersonResponse> map(List<Person> personList);
}

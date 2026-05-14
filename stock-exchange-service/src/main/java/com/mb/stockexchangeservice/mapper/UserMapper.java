package com.mb.stockexchangeservice.mapper;

import com.mb.stockexchangeservice.api.request.ApiUserRequest;
import com.mb.stockexchangeservice.api.response.ApiUserResponse;
import com.mb.stockexchangeservice.data.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    ApiUserResponse map(User user);

    List<ApiUserResponse> map(List<User> users);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdDateTime", ignore = true)
    @Mapping(target = "modifiedDateTime", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    User map(ApiUserRequest apiUserRequest);

    default User map(User oldRecord, User newRecord) {
        oldRecord.setFirstName(StringUtils.isNotBlank(newRecord.getFirstName()) ? newRecord.getFirstName() : oldRecord.getFirstName());
        oldRecord.setLastName(StringUtils.isNotBlank(newRecord.getLastName()) ? newRecord.getLastName() : oldRecord.getLastName());
        oldRecord.setUsername(StringUtils.isNotBlank(newRecord.getUsername()) ? newRecord.getUsername() : oldRecord.getUsername());
        oldRecord.setEmail(StringUtils.isNotBlank(newRecord.getEmail()) ? newRecord.getEmail() : oldRecord.getEmail());
        oldRecord.setPhoneNumber(StringUtils.isNotBlank(newRecord.getPhoneNumber()) ? newRecord.getPhoneNumber() : oldRecord.getPhoneNumber());
        return oldRecord;
    }

    default Page<ApiUserResponse> map(Page<User> orders) {
        return orders.map(this::map);
    }
}

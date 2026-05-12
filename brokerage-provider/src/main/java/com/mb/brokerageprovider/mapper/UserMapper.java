package com.mb.brokerageprovider.mapper;

import com.mb.brokerageprovider.api.request.ApiUserRequest;
import com.mb.brokerageprovider.api.response.ApiUserResponse;
import com.mb.brokerageprovider.data.entity.User;
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
    @Mapping(target = "orders", ignore = true)
    User map(ApiUserRequest apiUserRequest);

    default User map(User oldRecord, User newRecord) {
        oldRecord.setName(StringUtils.isNotBlank(newRecord.getName()) ? newRecord.getName() : oldRecord.getName());
        oldRecord.setSurname(StringUtils.isNotBlank(newRecord.getSurname()) ? newRecord.getSurname() : oldRecord.getSurname());
        oldRecord.setUsername(StringUtils.isNotBlank(newRecord.getUsername()) ? newRecord.getUsername() : oldRecord.getUsername());
        oldRecord.setEmail(StringUtils.isNotBlank(newRecord.getEmail()) ? newRecord.getEmail() : oldRecord.getEmail());
        return oldRecord;
    }

    default Page<ApiUserResponse> map(Page<User> orders) {
        return orders.map(this::map);
    }
}

package com.mb.brokerageprovider.mapper;

import com.mb.brokerageprovider.api.request.ApiUserRequest;
import com.mb.brokerageprovider.api.response.ApiUserResponse;
import com.mb.brokerageprovider.base.BaseUnitTest;
import com.mb.brokerageprovider.data.entity.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserMapperTest extends BaseUnitTest {

    UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void map_UserToApiUserResponse_ShouldSucceed() {
        // arrange
        User user = getUser();

        // act
        ApiUserResponse result = userMapper.map(user);

        // assertion

        assertEquals(user.getName(), result.getName());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void map_ListOfUserToListOfApiUserResponse_ShouldSucceed() {
        // arrange
        List<User> apiUserRequest = List.of(getUser());

        // act
        List<ApiUserResponse> result = userMapper.map(apiUserRequest);

        // assertion
        assertEquals(apiUserRequest.getFirst().getName(), result.getFirst().getName());
        assertEquals(apiUserRequest.getFirst().getUsername(), result.getFirst().getUsername());
        assertEquals(apiUserRequest.getFirst().getEmail(), result.getFirst().getEmail());
    }

    @Test
    void map_ApiUserRequestToUser_ShouldSucceed() {
        // arrange
        ApiUserRequest apiUserRequest = getApiUserRequest();

        // act
        User result = userMapper.map(apiUserRequest);

        // assertion
        assertEquals(apiUserRequest.getEmail(), result.getEmail());
        assertEquals(apiUserRequest.getUsername(), result.getUsername());
        assertEquals(apiUserRequest.getPhoneNumber(), result.getPhoneNumber());
    }

    @Test
    void map_UserWithPaginationToApiUserResponsePagination_ShouldSucceed() {
        // arrange
        List<User> users = List.of(getUser());

        final Page<User> userPage = new PageImpl<>(users, Pageable.ofSize(10), users.size());

        // act
        Page<ApiUserResponse> result = userMapper.map(userPage);

        // assertion
        assertEquals(userPage.getSize(), result.getSize());
        assertEquals(userPage.getContent().getFirst().getId(), result.getContent().getFirst().getId());
        assertEquals(userPage.getContent().getFirst().getEmail(), result.getContent().getFirst().getEmail());
        assertEquals(userPage.getContent().getFirst().getUsername(), result.getContent().getFirst().getUsername());
    }
}

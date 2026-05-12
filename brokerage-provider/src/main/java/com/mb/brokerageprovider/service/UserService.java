package com.mb.brokerageprovider.service;

import com.mb.brokerageprovider.data.entity.Order;
import com.mb.brokerageprovider.data.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    Page<User> getAllUsers(Pageable pageable);

    User createUser(User user);

    User getUserById(Long userId);

    User updateUserById(Long userId, User newUser);

    void deleteUserById(Long userId);

    List<Order> getAllOrdersByUserId(Long userId);
}

package com.mb.brokerageprovider.service.impl;

import com.mb.brokerageprovider.data.entity.Order;
import com.mb.brokerageprovider.data.entity.User;
import com.mb.brokerageprovider.data.repository.UserRepository;
import com.mb.brokerageprovider.exception.BaseException;
import com.mb.brokerageprovider.exception.BrokerageProviderErrorCode;
import com.mb.brokerageprovider.mapper.UserMapper;
import com.mb.brokerageprovider.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(BrokerageProviderErrorCode.USER_NOT_FOUND));
    }

    @Override
    public User updateUserById(Long userId, User newUser) {
        return userRepository.save(userMapper.map(getUserById(userId), newUser));
    }

    @Override
    public void deleteUserById(Long userId) {
        User user = getUserById(userId);
        userRepository.deleteById(user.getId());
    }

    @Override
    public List<Order> getAllOrdersByUserId(Long userId) {
        return getUserById(userId).getOrders();
    }
}

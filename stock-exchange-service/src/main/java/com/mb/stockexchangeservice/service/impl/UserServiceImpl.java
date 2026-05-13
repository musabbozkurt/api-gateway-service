package com.mb.stockexchangeservice.service.impl;

import com.mb.stockexchangeservice.data.entity.User;
import com.mb.stockexchangeservice.data.repository.RoleRepository;
import com.mb.stockexchangeservice.data.repository.UserRepository;
import com.mb.stockexchangeservice.exception.BaseException;
import com.mb.stockexchangeservice.exception.StockExchangeServiceErrorCode;
import com.mb.stockexchangeservice.mapper.UserMapper;
import com.mb.stockexchangeservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(roleRepository.findAllByDefaultRoleIsTrue());
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(StockExchangeServiceErrorCode.USER_NOT_FOUND));
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
}

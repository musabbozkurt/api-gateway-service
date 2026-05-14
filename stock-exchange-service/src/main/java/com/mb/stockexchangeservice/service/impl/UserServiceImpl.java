package com.mb.stockexchangeservice.service.impl;

import com.mb.stockexchangeservice.data.entity.User;
import com.mb.stockexchangeservice.data.repository.RoleRepository;
import com.mb.stockexchangeservice.data.repository.UserRepository;
import com.mb.stockexchangeservice.exception.BaseException;
import com.mb.stockexchangeservice.exception.StockExchangeServiceErrorCode;
import com.mb.stockexchangeservice.mapper.UserMapper;
import com.mb.stockexchangeservice.queue.QueueChannels;
import com.mb.stockexchangeservice.queue.event.UserCreatedEvent;
import com.mb.stockexchangeservice.queue.producer.EventProducer;
import com.mb.stockexchangeservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventProducer eventProducer;

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(roleRepository.findAllByDefaultRoleIsTrue());
        User savedUser = userRepository.save(user);

        UserCreatedEvent event = UserCreatedEvent.builder()
                .user(savedUser)
                .build();
        eventProducer.publishEvent(QueueChannels.USER_CREATED_EVENT_PRODUCER, event);
        log.info("Published UserCreatedEvent for user: {}", savedUser.getUsername());

        return savedUser;
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

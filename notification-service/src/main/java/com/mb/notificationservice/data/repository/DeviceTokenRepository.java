package com.mb.notificationservice.data.repository;

import com.mb.notificationservice.data.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByUserIdAndApplication(Long userId, String application);

    List<DeviceToken> findByUserIdAndActiveIsTrueAndApplicationIn(Long userId, Set<String> applications);
}

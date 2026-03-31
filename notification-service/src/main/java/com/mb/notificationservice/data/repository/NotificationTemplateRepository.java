package com.mb.notificationservice.data.repository;

import com.mb.notificationservice.data.entity.NotificationTemplate;
import com.mb.notificationservice.enums.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByCodeAndChannelAndActiveTrue(String code, NotificationChannel channel);

    Optional<NotificationTemplate> findByCodeAndChannel(String code, NotificationChannel channel);

    boolean existsByCodeAndChannel(String code, NotificationChannel channel);
}

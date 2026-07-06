package com.mb.notificationservice.data.repository;

import com.mb.notificationservice.data.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification>, ListPagingAndSortingRepository<Notification, Long> {

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndIsReadFalse(Long userId);

    @Query("""
            update Notification n
               set n.isRead = true,
                   n.readAt = CURRENT_TIMESTAMP
             where n.userId = :userId
               and n.isRead = false
            """)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    int updateUnreadToReadByUserId(Long userId);
}

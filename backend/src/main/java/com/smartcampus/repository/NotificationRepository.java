package com.smartcampus.repository;

import com.smartcampus.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByRecipientIdAndIsReadOrderByCreatedAtDesc(Long userId, boolean isRead);
    long countByRecipientIdAndIsRead(Long userId, boolean isRead);
}

package com.smartcampus.service;

import com.smartcampus.entity.Notification;
import com.smartcampus.entity.NotificationType;
import com.smartcampus.entity.User;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.NotificationRepository;
import com.smartcampus.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Notification createNotification(User recipient, String title, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getNotificationsForUserEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId()))
                .orElse(Collections.emptyList());
    }

    public List<Notification> getUnreadNotificationsForUser(Long userId) {
        return notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(userId, false);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsRead(userId, false);
    }

    public long getUnreadCountForUserEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> notificationRepository.countByRecipientIdAndIsRead(user.getId(), false))
                .orElse(0L);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(userId, false);
        unread.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void markAllAsReadForUserEmail(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            List<Notification> unread = notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(user.getId(), false);
            unread.forEach(notification -> notification.setRead(true));
            notificationRepository.saveAll(unread);
        });
    }
}

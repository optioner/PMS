package com.pm.service;

import com.pm.model.entity.Notification;
import com.pm.model.entity.User;
import com.pm.model.enums.NotificationType;
import com.pm.repository.NotificationRepository;
import com.pm.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public List<Notification> getMyNotifications() {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public long getUnreadCount() {
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        // Optional: Check ownership
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    public void markAllAsRead() {
        List<Notification> notifications = getMyNotifications();
        notifications.forEach(n -> {
            if (!n.isRead()) {
                n.setRead(true);
                n.setReadAt(LocalDateTime.now());
            }
        });
        notificationRepository.saveAll(notifications);
    }

    public void createNotification(User user, NotificationType type, String title, String content, String relatedType, Long relatedId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedType(relatedType);
        notification.setRelatedId(relatedId);
        notificationRepository.save(notification);
    }
}

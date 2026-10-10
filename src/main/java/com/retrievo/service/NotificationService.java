package com.retrievo.service;

import com.retrievo.entity.Notification;
import com.retrievo.entity.User;
import com.retrievo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public Notification createNotification(User recipient, String title, String message, String type, String targetUrl) {
        Notification notification = new Notification(recipient, title, message, type, targetUrl);
        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(User recipient) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipient.getId());
    }

    public long getUnreadCount(User recipient) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipient.getId());
    }

    @Transactional
    public void markAsRead(Long notificationId, User user) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found."));
        if (n.getRecipient().getId().equals(user.getId())) {
            n.setIsRead(true);
            notificationRepository.save(n);
        }
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> list = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId());
        list.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(list);
    }
}

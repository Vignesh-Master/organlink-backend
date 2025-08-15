package com.organlink.service;

import com.organlink.entity.User;
import com.organlink.entity.Notification;

import java.util.List;

public interface NotificationService {
    void createNotification(User user, String message, String link);
    List<Notification> getNotificationsForUser(Long userId);
    List<Notification> getUnreadNotificationsForUser(Long userId);
    void markAsRead(Long notificationId);
}

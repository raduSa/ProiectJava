package Entities;

import Utils.NotificationStatus;

public class Notification {
    private User user;
    private NotificationStatus status;

    public Notification(User user, NotificationStatus status) {
        this.user = user;
        this.status = status;
    }

    public String getNotification() {
        return user.getUsername() + " is " + status;
    }
}

package Entities;

import Utils.UserSessionUtils;

import java.time.LocalDateTime;

public class UserSession {
    private Integer sessionId = 1;
    private User user;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private String ipAddress;

    public UserSession(User user) {
        this.sessionId = ++UserSessionUtils.sessionId;
        this.user = user;
        this.loginTime = LocalDateTime.now();
        this.ipAddress = UserSessionUtils.generateRandomIP();
    }

    public Integer getSessionId() {
        return sessionId;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void logout() {
        this.logoutTime = LocalDateTime.now();
    }

    public boolean isActive() {
        return logoutTime == null;
    }

    @Override
    public String toString() {
        return "Session-" + sessionId + " for " + user.getUsername() +
                " from " + ipAddress + " started at " + loginTime +
                (logoutTime != null ? ", ended at " + logoutTime : ", active");
    }
}

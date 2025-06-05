package Entities;

import Utils.UserSessionUtils;

import java.time.LocalDateTime;

public class UserSession {
    private Integer sessionId;
    private User user;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private String ipAddress;

    public UserSession(User user) {
        this.user = user;
        this.loginTime = LocalDateTime.now();
        this.ipAddress = UserSessionUtils.generateRandomIP();
    }

    public UserSession(User user, Integer sessionId, LocalDateTime loginTime, LocalDateTime logoutTime, String ipAddress) {
        this.sessionId = sessionId;
        this.user = user;
        this.loginTime = loginTime;
        this.logoutTime = logoutTime;
        this.ipAddress = ipAddress;
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

    @Override
    public String toString() {
        return "Session-" + sessionId + " for " + user.getUsername() +
                " from " + ipAddress + " started at " + loginTime +
                (logoutTime != null ? ", ended at " + logoutTime : ", active");
    }
}

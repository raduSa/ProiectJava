package Entities;

import Utils.UserStatus;

public class User implements Comparable<User> {
    private String username;
    private UserStatus status;

    public User(String username) {
        this.username = username;
        this.status = UserStatus.OFFLINE;
    }

    public String getUsername() { return username; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    @Override
    public int compareTo(User other) {
        return this.username.compareTo(other.username);
    }

    @Override
    public String toString() {
        return username + " (" + status + ")";
    }
}

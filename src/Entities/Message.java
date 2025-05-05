package Entities;

import Utils.MessageStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Message {
    private String content;
    private User sender;
    private LocalDateTime timestamp;
    private Map<User, MessageStatus> deliveryStatus = new HashMap<>();

    public Message(String content, User sender) {
        this.content = content;
        this.sender = sender;
        this.timestamp = LocalDateTime.now();
    }

    public String getContent() { return content; }
    public User getSender() { return sender; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void markRead(User user) {
        deliveryStatus.put(user, MessageStatus.READ);
    }

    public MessageStatus getStatus(User user) {
        return deliveryStatus.getOrDefault(user, MessageStatus.SENT);
    }

    public void initializeStatus(Set<User> recipients) {
        for (User user : recipients) {
            if (!user.equals(sender)) {
                deliveryStatus.put(user, MessageStatus.RECEIVED);
            }
            else {
                deliveryStatus.put(user, MessageStatus.SENT);
            }
        }
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + sender.getUsername() + ": " + content;
    }
}

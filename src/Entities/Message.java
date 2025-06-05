package Entities;

import Utils.MessageStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Message {
    private int messageId;
    private String content;
    private User sender;
    private LocalDateTime timestamp;
    private Map<String, MessageStatus> deliveryStatus = new HashMap<>();

    public Message(String content, User sender) {
        this.content = content;
        this.sender = sender;
        this.timestamp = LocalDateTime.now();
    }

    public Message(String content, User sender, LocalDateTime timestamp, int messageId) {
        this.content = content;
        this.sender = sender;
        this.timestamp = timestamp;
        this.messageId = messageId;
    }

    public String getContent() { return content; }
    public User getSender() { return sender; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Map<String, MessageStatus> getDeliveryStatus() {
        return deliveryStatus;
    }

    public void markStatus(User user, MessageStatus status) {
        deliveryStatus.put(user.getUsername(), status);
    }

    public MessageStatus getStatus(String username) {
        return deliveryStatus.getOrDefault(username, null);
    }

    public void initializeStatus(Set<User> recipients) {
        for (User user : recipients) {
            if (!user.getUsername().equals(sender.getUsername())) {
                deliveryStatus.put(user.getUsername(), MessageStatus.RECEIVED);
            }
            else {
                deliveryStatus.put(user.getUsername(), MessageStatus.SENT);
            }
        }
    }

    @Override
    public String toString() {
        return messageId + ". " + "[" + timestamp + "] " + sender.getUsername() + ": " + content;
    }

    public int getId() {
        return messageId;
    }
}

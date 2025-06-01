package Repository;

import Config.Constants;
import Config.DatabaseConnection;
import Entities.ChatRoom;
import Entities.Message;
import Entities.User;
import Services.AuditService;
import Utils.MessageStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Singleton service that handles CRUD operations for Message entities
 */
public class MessageJdbcService {
    private static MessageJdbcService instance;
    private final AuditService auditService;
    private final UserJdbcService userService;
    
    private MessageJdbcService() {
        auditService = AuditService.getInstance();
        userService = UserJdbcService.getInstance();
    }
    
    public static MessageJdbcService getInstance() {
        if (instance == null) {
            instance = new MessageJdbcService();
        }
        return instance;
    }
    
    /**
     * Create a new message in the database
     * @param message The message to create
     * @param chatRoomId The ID of the chat room the message belongs to
     * @return The ID of the created message, or -1 if failed
     */
    public int createMessage(Message message, int chatRoomId) {
        String sql = "INSERT INTO " + Constants.MESSAGE_TABLE + 
                     " (content, sender_username, chatroom_id, timestamp) VALUES (?, ?, ?, ?)";
        int messageId = -1;
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setString(1, message.getContent());
            stmt.setString(2, message.getSender().getUsername());
            stmt.setInt(3, chatRoomId);
            stmt.setTimestamp(4, Timestamp.valueOf(message.getTimestamp()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        messageId = generatedKeys.getInt(1);

                        auditService.log("CREATE_MESSAGE");
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating message: " + e.getMessage());
        }
        
        return messageId;
    }
    
    /**
     * Save the delivery status for a message
     * @param messageId The ID of the message
     * @param message The message object with delivery status information
     */
    public void addMessageDeliveryStatus(int messageId, Message message) {
        String sql = "INSERT INTO " + Constants.MESSAGE_DELIVERY_TABLE + 
                     " (message_id, username, status) VALUES (?, ?, ?)";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            // For each user in the delivery status map
            for (String username : message.getDeliveryStatus().keySet()) {
                stmt.setInt(1, messageId);
                stmt.setString(2, username);
                stmt.setString(3, message.getStatus(username).toString());
                stmt.addBatch();
            }
            
            stmt.executeBatch();
            
        } catch (SQLException e) {
            System.err.println("Error saving message delivery status: " + e.getMessage());
        }
    }
    
    /**
     * Get a message by its ID
     * @param messageId The ID of the message
     * @return The Message if found, null otherwise
     */
    public Message getMessageById(int messageId) {
        String sql = "SELECT * FROM " + Constants.MESSAGE_TABLE + " WHERE id = ?";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, messageId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String content = rs.getString("content");
                    String senderUsername = rs.getString("sender_username");
                    LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                    
                    User sender = userService.getUserByUsername(senderUsername);
                    if (sender == null) {
                        sender = new User(senderUsername); // Fallback if user not found
                    }
                    
                    Message message = new Message(content, sender, timestamp, messageId);
                    
                    // Load delivery status
                    loadMessageDeliveryStatus(messageId, message);
                    
                    auditService.log("READ_MESSAGE");
                    return message;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving message: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Load the delivery status for a message
     * @param messageId The ID of the message
     * @param message The message object to update with delivery status
     */
    private void loadMessageDeliveryStatus(int messageId, Message message) {
        String sql = "SELECT username, status FROM " + Constants.MESSAGE_DELIVERY_TABLE + 
                     " WHERE message_id = ?";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, messageId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString("username");
                    String statusStr = rs.getString("status");
                    
                    User user = userService.getUserByUsername(username);
                    if (user == null) {
                        user = new User(username); // Fallback if user not found
                    }
                    
                    MessageStatus status = MessageStatus.valueOf(statusStr);

                    message.markStatus(user, status);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading message delivery status: " + e.getMessage());
        }
    }
    
    /**
     * Get all messages for a chat room
     * @param chatRoomId The ID of the chat room
     * @return List of messages in the chat room
     */
    public List<Message> getMessagesByChatRoomId(int chatRoomId) {
        String sql = "SELECT id FROM " + Constants.MESSAGE_TABLE + 
                     " WHERE chatroom_id = ? ORDER BY timestamp";
        List<Message> messages = new ArrayList<>();
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, chatRoomId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int messageId = rs.getInt("id");
                    Message message = getMessageById(messageId);
                    if (message != null) {
                        messages.add(message);
                    }
                }
            }
            
            auditService.log("READ_CHATROOM_MESSAGES");
            
        } catch (SQLException e) {
            System.err.println("Error retrieving messages for chat room: " + e.getMessage());
        }
        
        return messages;
    }
    
    /**
     * Update a message's content
     * @param messageId The ID of the message
     * @param newContent The new content for the message
     * @return True if successful
     */
    public boolean updateMessageContent(int messageId, String newContent) {
        String sql = "UPDATE " + Constants.MESSAGE_TABLE + " SET content = ? WHERE id = ?";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, newContent);
            stmt.setInt(2, messageId);
            
            int affectedRows = stmt.executeUpdate();
            auditService.log("UPDATE_MESSAGE");
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating message: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update a message's delivery status for a user
     * @param messageId The ID of the message
     * @param username The username of the user
     * @param status The new status
     * @return True if successful
     */
    public boolean updateMessageStatus(int messageId, String username, MessageStatus status) {
        String sql = "UPDATE " + Constants.MESSAGE_DELIVERY_TABLE + 
                     " SET status = ? WHERE message_id = ? AND username = ?";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, status.toString());
            stmt.setInt(2, messageId);
            stmt.setString(3, username);
            
            int affectedRows = stmt.executeUpdate();
            auditService.log("UPDATE_MESSAGE_STATUS");
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating message status: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete a message
     * @param messageId The ID of the message to delete
     * @return True if successful
     */
    public boolean deleteMessage(int messageId) {
        String sql = "DELETE FROM " + Constants.MESSAGE_TABLE + " WHERE id = ?";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, messageId);
            
            int affectedRows = stmt.executeUpdate();
            auditService.log("DELETE_MESSAGE");
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting message: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Initialize the delivery status for a set of recipients
     * @param messageId The ID of the message
     * @param recipients The set of recipients
     * @param sender The sender of the message
     * @return True if successful
     */
    public boolean initializeDeliveryStatus(int messageId, Set<User> recipients, User sender) {
        String sql = "INSERT INTO " + Constants.MESSAGE_DELIVERY_TABLE + 
                     " (message_id, username, status) VALUES (?, ?, ?)";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            for (User user : recipients) {
                stmt.setInt(1, messageId);
                stmt.setString(2, user.getUsername());
                
                if (user.equals(sender)) {
                    stmt.setString(3, MessageStatus.SENT.toString());
                } else {
                    stmt.setString(3, MessageStatus.RECEIVED.toString());
                }
                
                stmt.addBatch();
            }
            
            stmt.executeBatch();
            auditService.log("INITIALIZE_MESSAGE_STATUS");
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error initializing delivery status: " + e.getMessage());
            return false;
        }
    }
}

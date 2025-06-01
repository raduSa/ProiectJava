package Repository;

import Config.Constants;
import Config.DatabaseConnection;
import Entities.ChatRoom;
import Entities.GroupChat;
import Entities.PrivateChat;
import Entities.User;
import Services.AuditService;
import Utils.GroupPermission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Singleton service that handles CRUD operations for ChatRoom entities
 */
public class ChatRoomJdbcService {
    private static ChatRoomJdbcService instance;
    private final AuditService auditService;
    private final UserJdbcService userService;
    
    private ChatRoomJdbcService() {
        auditService = AuditService.getInstance();
        userService = UserJdbcService.getInstance();
    }
    
    public static ChatRoomJdbcService getInstance() {
        if (instance == null) {
            instance = new ChatRoomJdbcService();
        }
        return instance;
    }
    
    /**
     * Create a new chat room in the database
     * @param chatRoom The chat room to create
     * @param roomType The type of room ("GROUP" or "PRIVATE")
     * @param maxUsers Maximum number of users (for group chats)
     * @return The ID of the created room, or -1 if failed
     */
    public int createChatRoom(ChatRoom chatRoom, String roomType, Integer maxUsers) {
        String sql = "INSERT INTO " + Constants.CHATROOM_TABLE + " (name, room_type, max_users) VALUES (?, ?, ?)";
        int chatRoomId = -1;
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setString(1, chatRoom.getName());
            stmt.setString(2, roomType);
            stmt.setInt(3, maxUsers);

            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        chatRoomId = generatedKeys.getInt(1);
                        
                        // Add participants
                        for (User participant : chatRoom.getParticipants()) {
                            addParticipant(chatRoomId, participant.getUsername(), GroupPermission.OWNER);
                        }
                        
                        auditService.log("CREATE_CHATROOM");
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating chat room: " + e.getMessage());
        }
        
        return chatRoomId;
    }
    
    /**
     * Add a participant to a chat room
     * @param chatRoomId The ID of the chat room
     * @param username The username of the participant
     * @return True if successful
     */
    public boolean addParticipant(int chatRoomId, String username, GroupPermission permission) {
        String sql = "INSERT INTO " + Constants.PARTICIPANTS_TABLE + 
                     " (chatroom_id, username, permission) VALUES (?, ?, ?)";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, chatRoomId);
            stmt.setString(2, username);
            stmt.setString(3, permission.toString());
            
            int affectedRows = stmt.executeUpdate();
            auditService.log("ADD_PARTICIPANT");
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding participant: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Retrieve a chat room by its ID
     * @param chatRoomId The ID of the chat room
     * @return The ChatRoom if found, null otherwise
     */
    public ChatRoom getChatRoomById(int chatRoomId) {
        String sql = "SELECT * FROM " + Constants.CHATROOM_TABLE + " WHERE id = ?";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, chatRoomId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String roomType = rs.getString("room_type");
                    Integer maxUsers = rs.getObject("max_users", Integer.class);
                    
                    // Get participants
                    Set<User> participants = getParticipants(chatRoomId);
                    
                    ChatRoom chatRoom;
                    if (roomType.equals("GROUP")) {
                        // Create a group chat
                        User creator = null;
                        for (User user : participants) {
                            if (getParticipantPermission(chatRoomId, user.getUsername()) == GroupPermission.OWNER) {
                                creator = user;
                                break;
                            }
                        }

                        if (creator == null) {
                            creator = new User("UNKNOWN");
                        }

                        chatRoom = new GroupChat(name, creator, chatRoomId);
                        
                        // Add all participants
                        for (User participant : participants) {
                            if (participant != creator) {
                                ((GroupChat) chatRoom).addParticipant(participant);
                                
                                // Set permissions
                                GroupPermission permission = getParticipantPermission(chatRoomId, participant.getUsername());
                                if (permission != null) {
                                    ((GroupChat) chatRoom).setPermissions(participant, permission);
                                }
                            }
                        }
                    } else {
                        User[] users = participants.toArray(new User[0]);
                        chatRoom = new PrivateChat(users[0], users[1], chatRoomId);
                    }
                    
                    auditService.log("READ_CHATROOM");
                    return chatRoom;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving chat room: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get the permission of a participant in a chat room
     * @param chatRoomId The ID of the chat room
     * @param username The username of the participant
     * @return The permission string, or null if not found
     */
    public GroupPermission getParticipantPermission(int chatRoomId, String username) {
        String sql = "SELECT permission FROM " + Constants.PARTICIPANTS_TABLE + 
                     " WHERE chatroom_id = ? AND username = ?";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, chatRoomId);
            stmt.setString(2, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return GroupPermission.valueOf(rs.getString("permission"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving participant permission: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Update the permission of a participant in a chat room
     * @param chatRoomId The ID of the chat room
     * @param username The username of the participant
     * @param permission The new permission to set
     * @return True if successful, false otherwise
     */
    public boolean updateParticipantPermission(int chatRoomId, String username, GroupPermission permission) {
        String sql = "UPDATE " + Constants.PARTICIPANTS_TABLE + 
                     " SET permission = ? WHERE chatroom_id = ? AND username = ?";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, permission.toString());
            stmt.setInt(2, chatRoomId);
            stmt.setString(3, username);
            
            int affectedRows = stmt.executeUpdate();
            auditService.log("UPDATE_PARTICIPANT_PERMISSION");
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating participant permission: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all participants of a chat room
     * @param chatRoomId The ID of the chat room
     * @return Set of participants
     */
    public Set<User> getParticipants(int chatRoomId) {
        String sql = "SELECT username FROM " + Constants.PARTICIPANTS_TABLE + 
                     " WHERE chatroom_id = ?";
        Set<User> participants = new TreeSet<>();
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, chatRoomId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString("username");
                    User user = userService.getUserByUsername(username);
                    if (user != null) {
                        participants.add(user);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving participants: " + e.getMessage());
        }
        
        return participants;
    }
    
    /**
     * Get all chat rooms
     * @return List of all chat rooms names
     */
    public List<String> getAllChatRooms() {
        String sql = "SELECT name FROM " + Constants.CHATROOM_TABLE;
        List<String> chatRooms = new ArrayList<>();
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                String chatRoomName = rs.getString("name");
                chatRooms.add(chatRoomName);
            }
            
            auditService.log("READ_ALL_CHATROOMS");
            
        } catch (SQLException e) {
            System.err.println("Error retrieving all chat rooms: " + e.getMessage());
        }
        
        return chatRooms;
    }

    /**
     * Get all chat rooms
     * @return List of all chat rooms names
     */
    public List<String> getChatRoomsWithMember(String username) {
        String sql = "SELECT name, chatroom_id FROM " + Constants.CHATROOM_TABLE + " r JOIN "
                    + Constants.PARTICIPANTS_TABLE + " p ON (r.id = p.chatroom_id) "
                    + "WHERE p.username = ?";
        List<String> chatRooms = new ArrayList<>();

        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String chatRoomName = rs.getString("name");
                    int chatRoomid = rs.getInt("chatroom_id");
                    chatRooms.add(chatRoomid + ". " + chatRoomName);
                }
            }

            auditService.log("READ_CHATROOMS_WITH_MEMBER");

        } catch (SQLException e) {
            System.err.println("Error retrieving chat rooms for user " + username + ": " + e.getMessage());
        }

        return chatRooms;
    }
    
    /**
     * Update a chat room's name
     * @param chatRoomId The ID of the chat room
     * @param newName The new name for the chat room
     * @return True if successful
     */
    public boolean updateChatRoomName(int chatRoomId, String newName) {
        String sql = "UPDATE " + Constants.CHATROOM_TABLE + " SET name = ? WHERE id = ?";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, newName);
            stmt.setInt(2, chatRoomId);
            
            int affectedRows = stmt.executeUpdate();
            auditService.log("UPDATE_CHATROOM");
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating chat room: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete a chat room
     * @param chatRoomId The ID of the chat room to delete
     * @return True if successful
     */
    public boolean deleteChatRoom(int chatRoomId) {
        String sql = "DELETE FROM " + Constants.CHATROOM_TABLE + " WHERE id = ?";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, chatRoomId);
            
            int affectedRows = stmt.executeUpdate();
            auditService.log("DELETE_CHATROOM");
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting chat room: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Remove a participant from a chat room
     * @param chatRoomId The ID of the chat room
     * @param username The username of the participant to remove
     * @return True if successful
     */
    public boolean removeParticipant(int chatRoomId, String username) {
        String sql = "DELETE FROM " + Constants.PARTICIPANTS_TABLE + 
                     " WHERE chatroom_id = ? AND username = ?";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, chatRoomId);
            stmt.setString(2, username);
            
            int affectedRows = stmt.executeUpdate();
            auditService.log("REMOVE_PARTICIPANT");
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error removing participant: " + e.getMessage());
            return false;
        }
    }
}

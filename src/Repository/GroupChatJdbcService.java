package Repository;

import Config.Constants;
import Config.DatabaseConnection;
import Entities.GroupChat;
import Entities.User;
import Services.AuditService;
import Utils.GroupPermission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton service that handles CRUD operations for GroupChat entities
 */
public class GroupChatJdbcService {
    private static GroupChatJdbcService instance;
    private final AuditService auditService;
    private final ChatRoomJdbcService chatRoomService;
    private final UserJdbcService userService;
    
    private GroupChatJdbcService() {
        auditService = AuditService.getInstance();
        chatRoomService = ChatRoomJdbcService.getInstance();
        userService = UserJdbcService.getInstance();
    }
    
    public static GroupChatJdbcService getInstance() {
        if (instance == null) {
            instance = new GroupChatJdbcService();
        }
        return instance;
    }
    
    /**
     * Create a new group chat in the database
     * @param groupChat The group chat to create
     * @return The ID of the created group chat, or -1 if failed
     */
    public int createGroupChat(GroupChat groupChat) {
        // Create the group chat directly in the combined table
        int chatRoomId = chatRoomService.createChatRoom(groupChat, "GROUP", 50);
        
        if (chatRoomId == -1) {
            return -1;
        }
        
        // Set the creator as OWNER
        for (User user : groupChat.getParticipants()) {
            if (groupChat.getPermission(user) == GroupPermission.OWNER) {
                updateParticipantPermission(chatRoomId, user.getUsername(), GroupPermission.OWNER);
                break;
            }
        }
        
        auditService.log("CREATE_GROUP_CHAT");
        return chatRoomId;
    }
    
    /**
     * Get a group chat by its ID
     * @param groupChatId The ID of the group chat
     * @return The GroupChat if found, null otherwise
     */
    public GroupChat getGroupChatById(int groupChatId) {
        // Check if it's a group chat
        String sql = "SELECT * FROM " + Constants.CHATROOM_TABLE + 
                     " WHERE id = ? AND room_type = 'GROUP'";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, groupChatId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // It is a group chat, get the chat room
                    return (GroupChat) chatRoomService.getChatRoomById(groupChatId);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving group chat: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get all group chats
     * @return List of all group chats
     */
    public List<GroupChat> getAllGroupChats() {
        List<GroupChat> groupChats = new ArrayList<>();
        String sql = "SELECT id FROM " + Constants.CHATROOM_TABLE + " WHERE room_type = 'GROUP'";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                int chatRoomId = rs.getInt("id");
                GroupChat groupChat = getGroupChatById(chatRoomId);
                
                if (groupChat != null) {
                    groupChats.add(groupChat);
                }
            }
            
            auditService.log("READ_ALL_GROUP_CHATS");
            
        } catch (SQLException e) {
            System.err.println("Error retrieving all group chats: " + e.getMessage());
        }
        
        return groupChats;
    }
    
    /**
     * Update a participant's permission in a group chat
     * @param groupChatId The ID of the group chat
     * @param username The username of the participant
     * @param permission The new permission
     * @return True if successful
     */
    public boolean updateParticipantPermission(int groupChatId, String username, GroupPermission permission) {
        String sql = "UPDATE " + Constants.PARTICIPANTS_TABLE + 
                     " SET permission = ? WHERE chatroom_id = ? AND username = ?";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, permission.toString());
            stmt.setInt(2, groupChatId);
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
     * Update the maximum number of users in a group chat
     * @param groupChatId The ID of the group chat
     * @param maxUsers The new maximum number of users
     * @return True if successful
     */
    public boolean updateMaxUsers(int groupChatId, int maxUsers) {
        String sql = "UPDATE " + Constants.CHATROOM_TABLE + 
                     " SET max_users = ? WHERE id = ? AND room_type = 'GROUP'";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, maxUsers);
            stmt.setInt(2, groupChatId);
            
            int affectedRows = stmt.executeUpdate();
            auditService.log("UPDATE_GROUP_CHAT_MAX_USERS");
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating max users: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete a group chat
     * @param groupChatId The ID of the group chat to delete
     * @return True if successful
     */
    public boolean deleteGroupChat(int groupChatId) {
        // Delete directly from the chatrooms table
        boolean success = chatRoomService.deleteChatRoom(groupChatId);
        if (success) {
            auditService.log("DELETE_GROUP_CHAT");
        }
        return success;
    }
    
//    /**
//     * Add a participant to a group chat
//     * @param groupChatId The ID of the group chat
//     * @param username The username of the participant to add
//     * @return True if successful
//     */
//    public boolean addParticipant(int groupChatId, String username) {
//        User user = userService.getUserByUsername(username);
//        if (user == null) {
//            return false;
//        }
//
//        boolean success = chatRoomService.addParticipant(groupChatId, username, GroupPermission.MEMBER.toString());
//        if (success) {
//            auditService.log("ADD_GROUP_CHAT_PARTICIPANT");
//        }
//        return success;
//    }
    
    /**
     * Remove a participant from a group chat
     * @param groupChatId The ID of the group chat
     * @param username The username of the participant to remove
     * @return True if successful
     */
    public boolean removeParticipant(int groupChatId, String username) {
        boolean success = chatRoomService.removeParticipant(groupChatId, username);
        if (success) {
            auditService.log("REMOVE_GROUP_CHAT_PARTICIPANT");
        }
        return success;
    }
}

import Config.DatabaseConnection;
import Entities.*;
import Repository.*;
import Services.AuditService;
import Utils.GroupPermission;
import Utils.UserStatus;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import java.sql.SQLException;

/**
 * Demo class showing how to use the JDBC services with audit functionality
 */
public class JdbcDemo {

    public static void main(String[] args) throws SQLException {
            
        // Get service instances
        UserJdbcService userService = UserJdbcService.getInstance();
        ChatRoomJdbcService chatRoomService = ChatRoomJdbcService.getInstance();
        GroupChatJdbcService groupChatService = GroupChatJdbcService.getInstance();
        MessageJdbcService messageService = MessageJdbcService.getInstance();
        UserSessionJdbcService sessionService = UserSessionJdbcService.getInstance();
        AuditService auditService = AuditService.getInstance();

        System.out.println("JDBC Services initialized successfully");

        // Demo user CRUD operations
        demoUserOperations(userService);

        // Demo group chat operations
        demoGroupChatOperations(groupChatService, userService);
//
        // Demo message operations
        demoMessageOperations(messageService, groupChatService, userService);
//
        // Demo user session operations
        demoUserSessionOperations(sessionService, userService);

        System.out.println("Demo completed successfully. Check audit.csv for logged operations.");
    }
    
    private static void demoUserOperations(UserJdbcService userService) {
        System.out.println("\n--- User CRUD Operations ---");
        
        // Create users
        User alice = new User("alice");
        User bob = new User("bob");
        User charlie = new User("charlie");
        
        System.out.println("Creating users...");
        userService.createUser(alice);
        userService.createUser(bob);
        userService.createUser(charlie);
        
        // Read user
        System.out.println("Reading user by username...");
        User retrievedUser = userService.getUserByUsername("alice");
        System.out.println("Retrieved user: " + retrievedUser);
        
        // Read all users
        System.out.println("Reading all users...");
        List<User> allUsers = userService.getAllUsers();
        System.out.println("Total users: " + allUsers.size());
        for (User user : allUsers) {
            System.out.println("- " + user);
        }
        
        // Update user
        System.out.println("Updating user status...");
        alice.setStatus(UserStatus.ONLINE);
        userService.updateUser(alice);
        
        User updatedUser = userService.getUserByUsername("alice");
        System.out.println("Updated user: " + updatedUser);
        
        // Delete user (comment out if you want to keep the user for other demos)
        /*
        System.out.println("Deleting user...");
        userService.deleteUser("charlie");
        System.out.println("User deleted.");
        */
    }
    
    private static void demoGroupChatOperations(GroupChatJdbcService groupChatService, UserJdbcService userService) {
        System.out.println("\n--- Group Chat CRUD Operations ---");
        
        // Get users
        User alice = userService.getUserByUsername("alice");
        User bob = userService.getUserByUsername("bob");
        
        // Create group chat
        System.out.println("Creating group chat...");
        GroupChat studyGroup = new GroupChat("Study Group", alice);
        studyGroup.setPermissions(alice, GroupPermission.OWNER);
        
        int groupChatId = groupChatService.createGroupChat(studyGroup);
        System.out.println("Group chat created with ID: " + groupChatId);
        
        // Add participant
        System.out.println("Adding participant to group chat...");
        groupChatService.addParticipant(groupChatId, bob.getUsername());
        
        // Update participant permission
        System.out.println("Updating participant permission...");
        groupChatService.updateParticipantPermission(groupChatId, bob.getUsername(), GroupPermission.ADMIN);
        
        // Read group chat
        System.out.println("Reading group chat...");
        GroupChat retrievedGroup = groupChatService.getGroupChatById(groupChatId);
        System.out.println("Retrieved group: " + retrievedGroup.getName());
        System.out.println("Participants: " + retrievedGroup.getParticipants().size());
        
        // Update group chat name
        System.out.println("Updating group chat name...");
        ChatRoomJdbcService chatRoomService = ChatRoomJdbcService.getInstance();
        chatRoomService.updateChatRoomName(groupChatId, "Advanced Study Group");
        
        retrievedGroup = groupChatService.getGroupChatById(groupChatId);
        System.out.println("Updated group name: " + retrievedGroup.getName());
    }
    
    private static void demoMessageOperations(MessageJdbcService messageService, 
                                             GroupChatJdbcService groupChatService,
                                             UserJdbcService userService) {
        System.out.println("\n--- Message CRUD Operations ---");
        
        // Get users and group chat
        User alice = userService.getUserByUsername("alice");
        User bob = userService.getUserByUsername("bob");
        
        // Find the first group chat (assuming we just created one in the previous demo)
        List<ChatRoom> chatRooms = ChatRoomJdbcService.getInstance().getAllChatRooms();
        
        int chatRoomId = 1; // Assuming the first group chat has ID 1
        
        // Create messages
        System.out.println("Creating messages...");
        Message aliceMsg = new Message("Hello everyone!", alice);
        aliceMsg.initializeStatus(chatRooms.get(0).getParticipants());
        
        int messageId = messageService.createMessage(aliceMsg, chatRoomId);
        System.out.println("Message created with ID: " + messageId);
        
        Message bobMsg = new Message("Hi Alice, how are you?", bob);
        bobMsg.initializeStatus(chatRooms.get(0).getParticipants());
        
        int bobMsgId = messageService.createMessage(bobMsg, chatRoomId);
        System.out.println("Second message created with ID: " + bobMsgId);
        
        // Read message
        System.out.println("Reading message...");
        Message retrievedMsg = messageService.getMessageById(messageId);
        System.out.println("Retrieved message: " + retrievedMsg.getContent());
        System.out.println("Sender: " + retrievedMsg.getSender().getUsername());
        
        // Read all messages in chat room
        System.out.println("Reading all messages in chat room...");
        List<Message> chatMessages = messageService.getMessagesByChatRoomId(chatRoomId);
        System.out.println("Total messages: " + chatMessages.size());
        for (Message msg : chatMessages) {
            System.out.println("- " + msg);
        }
        
        // Update message status
        System.out.println("Updating message status...");
        messageService.updateMessageStatus(messageId, bob.getUsername(), Utils.MessageStatus.READ);
        
        // Update message content
        System.out.println("Updating message content...");
        messageService.updateMessageContent(messageId, "Hello everyone! Welcome to the study group!");
        
        retrievedMsg = messageService.getMessageById(messageId);
        System.out.println("Updated message content: " + retrievedMsg.getContent());
    }
    
    private static void demoUserSessionOperations(UserSessionJdbcService sessionService, UserJdbcService userService) {
        System.out.println("\n--- User Session CRUD Operations ---");
        
        // Get users
        User alice = userService.getUserByUsername("alice");
        User bob = userService.getUserByUsername("bob");
        
        if (alice == null || bob == null) {
            System.out.println("Users not found. Make sure to run user demo first.");
            return;
        }
        
        // Create user sessions
        System.out.println("Creating user sessions...");
        UserSession aliceSession = new UserSession(alice);
        UserSession bobSession = new UserSession(bob);
        
        int aliceSessionId = sessionService.createUserSession(aliceSession);
        int bobSessionId = sessionService.createUserSession(bobSession);
        
        System.out.println("Alice session created with ID: " + aliceSessionId);
        System.out.println("Bob session created with ID: " + bobSessionId);
        
        // Read user session
        System.out.println("Reading user session...");
        UserSession retrievedSession = sessionService.getUserSessionById(aliceSessionId);
        System.out.println("Retrieved session: " + retrievedSession);
        
        // Read user sessions by username
        System.out.println("Reading user sessions by username...");
        List<UserSession> aliceSessions = sessionService.getUserSessionsByUsername(alice.getUsername());
        System.out.println("Alice has " + aliceSessions.size() + " session(s)");
        
        // Read active sessions
        System.out.println("Reading active sessions...");
        List<UserSession> activeSessions = sessionService.getActiveSessions();
        System.out.println("Total active sessions: " + activeSessions.size());
        
        // End user session
        System.out.println("Ending Bob's session...");
        sessionService.endUserSession(bobSessionId);
        
        // Check active sessions again
        activeSessions = sessionService.getActiveSessions();
        System.out.println("Total active sessions after ending Bob's: " + activeSessions.size());
        
        // Delete user session
        System.out.println("Deleting Alice's session...");
        sessionService.deleteUserSession(aliceSessionId);
        
        // Check all sessions
        List<UserSession> allSessions = new ArrayList<>();
        allSessions.addAll(sessionService.getUserSessionsByUsername(alice.getUsername()));
        allSessions.addAll(sessionService.getUserSessionsByUsername(bob.getUsername()));
        System.out.println("Total remaining sessions: " + allSessions.size());
    }
}

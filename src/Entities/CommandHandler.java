package Entities;

import Repository.ChatRoomJdbcService;
import Services.AuditService; // Added import
import Services.ChatService;
import Services.SessionService;
import Services.UserService;

import java.nio.channels.SeekableByteChannel;
import java.util.List;
import java.util.Objects;

public class CommandHandler {
    private String[] tokens;
    private String input;
    ChatService chatService;
    SessionService sessionService;
    UserService userService = null;
    private final AuditService auditService; // Added AuditService field

    public CommandHandler(ChatService chatService, SessionService sessionService) {
        this.chatService = chatService;
        this.sessionService = sessionService;
        this.auditService = AuditService.getInstance(); // Initialize AuditService
    }

    public void handleCommand() {
        User user;
        ChatRoom room;

        try {

            switch (tokens[0]) {
                // SHOW_ALL USERS / ROOMS / ACTIVE_SESSIONS / INACTIVE_SESSIONS
                case "SHOW_ALL":
                    String showAllType = tokens.length > 1 ? tokens[1] : "UNKNOWN";
                    auditService.log("SHOW_ALL", "type: " + showAllType);
                    if (Objects.equals(tokens[1], "USERS")) {
                        chatService.getUsers().forEach(System.out::println);
                    } else if (Objects.equals(tokens[1], "ROOMS")) {
                        chatService.getChatRoomNames().forEach(System.out::println);
                    } else if (Objects.equals(tokens[1], "ACTIVE_SESSIONS")) {
                        sessionService.showActiveSessions();
                    } else if (Objects.equals(tokens[1], "INACTIVE_SESSIONS")) {
                        sessionService.showInactiveSessions();
                    } else {
                        System.out.println("Unknown command");
                    }
                    break;
                // REGISTER [username]
                case "REGISTER":
                    auditService.log("REGISTER", "username: " + tokens[1]);
                    user = chatService.registerUser(tokens[1]);
                    break;
                // LOGIN [username]
                case "LOGIN":
                    auditService.log("LOGIN", "username: " + tokens[1]);
                    user = chatService.getUserByName(tokens[1]);
                    if (!checkUserExists(user)) break;
                    userService = new UserService(user);
                    userService.login();
                    sessionService.login(user);
                    System.out.printf("Logged in as user: %s", userService.getUser());
                    break;
                // LOGOUT
                case "LOGOUT":
                    if (!checkLoggedIn()) break;
                    auditService.log("LOGOUT", "user: " + (userService != null && userService.getUser() != null ? userService.getUser().getUsername() : "N/A"));
                    userService.logout();
                    sessionService.logout(userService.getUser());
                    userService = null;
                    System.out.println("Logged out");
                    break;
                // SHOW ROOMS / MSG [room] / PARTICIPANTS [room]
                case "SHOW":
                    if (!checkLoggedIn()) break;
                    String showType = tokens.length > 1 ? tokens[1] : "UNKNOWN";
                    String showDetails = tokens.length > 2 ? ", details: " + tokens[2] : "";
                    auditService.log("SHOW", "type: " + showType + showDetails + ", user: " + userService.getUser().getUsername());
                    if (Objects.equals(tokens[1], "ROOMS")) {
                        System.out.println("User is part of the following rooms:");
                        chatService.getChatRoomsWithMember(userService.getUser());
                    } else if (Objects.equals(tokens[1], "MSG")) {
                        System.out.printf("Messages from room %s: \n", tokens[2]);
                        chatService.getChatHistory(Integer.parseInt(tokens[2]));
                    } else if (Objects.equals(tokens[1], "PARTICIPANTS")) {
                        System.out.printf("Participants of room %s: \n", tokens[2]);
                        chatService.getChatParticipants(Integer.parseInt(tokens[2]));
                    } else if (Objects.equals(tokens[1], "EMPTY_SLOTS")) {
                        chatService.showEmptySlots(Integer.parseInt(tokens[2]));
                    } else {
                        System.out.println("Unknown command");
                    }
                    break;
                // SEND [room] [msg]
                case "SEND":
                    if (!checkLoggedIn()) break;
                    auditService.log("SEND", "room: " + tokens[1] + ", user: " + userService.getUser().getUsername() + ", message: " + tokens[2]);
                    room = chatService.getRoomById(Integer.parseInt(tokens[1]));
                    if (!checkRoomExists(room)) break;
                    chatService.sendMessage(room, userService.getUser(), tokens[2]);
                    break;
                // ADD_TO [room] [username]
                case "ADD_TO":
                    if (!checkLoggedIn()) break;
                    auditService.log("ADD_TO", "room: " + tokens[1] + ", userToAdd: " + tokens[2] + ", byUser: " + userService.getUser().getUsername());
                    room = chatService.getRoomById(Integer.parseInt(tokens[1]));
                    user = chatService.getUserByName(tokens[2]);
                    if (!checkRoomIsGroup(room)) break;
                    if (!checkUserExists(user)) break;
                    userService.addUserToGroup((GroupChat) room, user);
                    break;
                // KICK [room] [username]
                case "KICK":
                    if (!checkLoggedIn()) break;
                    auditService.log("KICK", "room: " + tokens[1] + ", userToKick: " + tokens[2] + ", byUser: " + userService.getUser().getUsername());
                    room = chatService.getRoomById(Integer.parseInt(tokens[1]));
                    user = chatService.getUserByName(tokens[2]);
                    if (!checkRoomIsGroup(room)) break;
                    if (!checkUserExists(user)) break;
                    userService.kickUserFromGroup((GroupChat) room, user);
                    break;
                // CREATE GROUP [name] / PRIVATE [username]
                case "CREATE":
                    if (!checkLoggedIn()) break;
                    String createType = tokens.length > 1 ? tokens[1] : "UNKNOWN";
                    String createDetails = tokens.length > 2 ? ", name/user: " + tokens[2] : "";
                    auditService.log("CREATE", "type: " + createType + createDetails + ", createdBy: " + userService.getUser().getUsername());
                    if (Objects.equals(tokens[1], "GROUP")) {
                        List<String> groups = ChatRoomJdbcService.getInstance().getChatRoomsWithMember(userService.getUser().getUsername());
                        // Note: groups.contains(tokens[1]) might be a bug if tokens[1] is "GROUP" and not the group name tokens[2]
                        // Assuming the intent was to check tokens[2] (group name)
                        if (groups.contains(tokens[2])) {
                            System.out.println("User is already part of a group with this name!");
                            break;
                        }
                        chatService.createGroupChat(tokens[2], userService.getUser());
                    } else if (Objects.equals(tokens[1], "PRIVATE")) {
                        User otherUser = chatService.getUserByName(tokens[2]);
                        if (!checkUserExists(otherUser)) break;
                        chatService.createPrivateChat(userService.getUser(), otherUser);
                    } else {
                        System.out.println("Unknown command");
                    }
                    break;
                // SEARCH [room] [keyword]
                case "SEARCH":
                    if (!checkLoggedIn()) break;
                    auditService.log("SEARCH", "room: " + tokens[1] + ", keyword: " + tokens[2] + ", user: " + userService.getUser().getUsername());
                    room = chatService.getRoomById(Integer.parseInt(tokens[1]));
                    String keyword = tokens[2];
                    if (!checkRoomExists(room)) break;
                    chatService.searchMessages(room, keyword);
                    break;
                // MSG_STATUS [room]
                case "MSG_STATUS":
                    if (!checkLoggedIn()) break;
                    auditService.log("MSG_STATUS", "room: " + tokens[1] + ", user: " + userService.getUser().getUsername());
                    room = chatService.getRoomById(Integer.parseInt(tokens[1]));
                    if (!checkRoomExists(room)) break;
                    userService.showMessageStatus(room);
                    break;
                // READ [room]
                case "READ":
                    if (!checkLoggedIn()) break;
                    auditService.log("READ", "room: " + tokens[1] + ", user: " + userService.getUser().getUsername());
                    room = chatService.getRoomById(Integer.parseInt(tokens[1]));
                    if (!checkRoomExists(room)) break;
                    userService.simulateReading(room);
                    break;
                // UNREAD [room]
                case "UNREAD":
                    if (!checkLoggedIn()) break;
                    auditService.log("UNREAD", "room: " + tokens[1] + ", user: " + userService.getUser().getUsername());
                    room = chatService.getRoomById(Integer.parseInt(tokens[1]));
                    if (!checkRoomExists(room)) break;
                    userService.showUnreadMessages(room);
                    break;
                // UNREAD_CNT [room]
                case "UNREAD_CNT":
                    if (!checkLoggedIn()) break;
                    auditService.log("UNREAD_CNT", "room: " + tokens[1] + ", user: " + userService.getUser().getUsername());
                    room = chatService.getRoomById(Integer.parseInt(tokens[1]));
                    if (!checkRoomExists(room)) break;
                    System.out.println(userService.getUnreadMessageCount(room));
                    break;
                // ADMIN [room] [username]
                case "ADMIN":
                    if (!checkLoggedIn()) break;
                    auditService.log("ADMIN", "room: " + tokens[1] + ", userToMakeAdmin: " + tokens[2] + ", byUser: " + userService.getUser().getUsername());
                    room = chatService.getRoomById(Integer.parseInt(tokens[1]));
                    user = chatService.getUserByName(tokens[2]);
                    if (!checkRoomIsGroup(room)) break;
                    if (!checkUserExists(user)) break;
                    userService.makeUserAdmin((GroupChat) room, user);
                    break;
                // REM_ADMIN [room] [username]
                case "REM_ADMIN":
                    if (!checkLoggedIn()) break;
                    auditService.log("REM_ADMIN", "room: " + tokens[1] + ", userToRemoveAdmin: " + tokens[2] + ", byUser: " + userService.getUser().getUsername());
                    room = chatService.getRoomById(Integer.parseInt(tokens[1]));
                    user = chatService.getUserByName(tokens[2]);
                    if (!checkRoomIsGroup(room)) break;
                    if (!checkUserExists(user)) break;
                    userService.removeUserAdmin((GroupChat) room, user);
                    break;
                // ROLES [room]
                case "ROLES":
                    auditService.log("ROLES", "room: " + tokens[1] + (userService != null && userService.getUser() != null ? ", requestedBy: " + userService.getUser().getUsername() : ""));
                    room = chatService.getRoomById(Integer.parseInt(tokens[1]));
                    if (!checkRoomIsGroup(room)) break;
                    ((GroupChat) room).showPermissions();
                    break;
                case "EDIT":
                    if (!checkLoggedIn()) break;
                    auditService.log("EDIT", "messageId: " + tokens[1] + ", newContent: " + tokens[2] + ", user: " + userService.getUser().getUsername());
                    userService.updateMessageContent(Integer.parseInt(tokens[1]), tokens[2]);
                    break;
                case "DELETE":
                    if (!checkLoggedIn()) break;
                    String deleteType = tokens.length > 1 ? tokens[1] : "UNKNOWN";
                    String deleteDetails = "";
                    if (tokens.length > 2) {
                        deleteDetails = ", target: " + tokens[2];
                    }
                    auditService.log("DELETE", "type: " + deleteType + deleteDetails + ", user: " + userService.getUser().getUsername());
                    if (Objects.equals(tokens[1], "MSG")) {
                        userService.updateMessageContent(Integer.parseInt(tokens[2]), "DELETED");
                    } else if (Objects.equals(tokens[1], "GROUP")) {
                        room = chatService.getRoomById(Integer.parseInt(tokens[2]));
                        if (!checkRoomExists(room)) break;
                        userService.deleteGroup(Integer.parseInt(tokens[2]));
                    } else {
                        System.out.println("Unknown command");
                    }
                    break;
                default:
                    auditService.log(tokens[0], "Unknown or unhandled command" + (userService != null && userService.getUser() != null ? ", attemptedBy: " + userService.getUser().getUsername() : ""));
                    System.out.println("Unknown command");
            }
        }
        catch (Error e) {
            System.out.println("Unknown command - " + e.getMessage());
        }
    }

    public void readCommand(String input) {
        tokens = input.split(" ", 3);
    }

    private Boolean checkLoggedIn() {
        if (userService == null) {
            System.out.println("Not logged in!");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
    
    private Boolean checkUserExists(User user) {
        if (user == null) {
            System.out.println("User given does not exist!");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private Boolean checkRoomExists(ChatRoom room) {
        if (room == null) {
            System.out.println("Room given does not exist!");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
    
    private Boolean checkRoomIsGroup(ChatRoom room) {
        if (!(room instanceof GroupChat)) {
            System.out.println("Room given is not a group chat!");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}

package Entities;

import Services.ChatService;
import Services.SessionService;
import Services.UserService;

import java.nio.channels.SeekableByteChannel;
import java.util.Objects;

public class CommandHandler {
    private String[] tokens;
    private String input;
    ChatService service;
    SessionService sessionService;
    UserService userService = null;

    public CommandHandler(ChatService chatService, SessionService sessionService) {
        service = chatService;
        this.sessionService = sessionService;
    }

    public void handleCommand() {
        User user;
        ChatRoom room;

        switch(tokens[0]) {
            // SHOW_ALL USERS / ROOMS / ACTIVE_SESSIONS / INACTIVE_SESSIONS
            case "SHOW_ALL":
                if (Objects.equals(tokens[1], "USERS")) {
                    service.getUsers().values().forEach(System.out::println);
                }
                else if (Objects.equals(tokens[1], "ROOMS")) {
                    service.getChatRooms().forEach(System.out::println);
                }
                else if (Objects.equals(tokens[1], "ACTIVE_SESSIONS")) {
                    sessionService.showActiveSessions();
                }
                else if (Objects.equals(tokens[1], "INACTIVE_SESSIONS")) {
                    sessionService.showInactiveSessions();
                }
                else {
                    System.out.println("Unknown command");
                }
                break;
            // REGISTER [username]
            case "REGISTER":
                user = service.registerUser(tokens[1]);
                break;
            // LOGIN [username]
            case "LOGIN":
                user = service.getUsers().get(tokens[1]);
                if (!checkUserExists(user)) break;
                userService = new UserService(user);
                userService.login();
                sessionService.login(user);
                System.out.printf("Logged in as user: %s", userService.getUser());
                break;
            // LOGOUT
            case "LOGOUT":
                if(!checkLoggedIn()) break;
                userService.logout();
                sessionService.logout(userService.getUser());
                userService = null;
                System.out.println("Logged out");
                break;
            // SHOW ROOMS / MSG [room] / PARTICIPANTS [room]
            case "SHOW":
                if(!checkLoggedIn()) break;
                if (Objects.equals(tokens[1], "ROOMS")) {
                    System.out.println("User is part of the following rooms:");
                    service.getChatRooms().stream().filter(chatRoom -> chatRoom.getParticipants().
                            contains(userService.getUser())).forEach(System.out::println);
                }
                else if (Objects.equals(tokens[1], "MSG")) {
                    System.out.printf("Messages from room %s: \n", tokens[2]);
                    service.getChatHistory(service.getRoomByName(tokens[2])).forEach(System.out::println);
                }
                else if (Objects.equals(tokens[1], "PARTICIPANTS")) {
                    System.out.printf("Participants of room %s: \n", tokens[2]);
                    service.getChatParticipants(service.getRoomByName(tokens[2])).forEach(System.out::println);
                }
                else if (Objects.equals(tokens[1], "EMPTY_SLOTS")) {
                    service.showEmptySlots(service.getRoomByName(tokens[2]));
                }
                else {
                    System.out.println("Unknown command");
                }
                break;
            // SEND [room] [msg]
            case "SEND":
                if(!checkLoggedIn()) break;
                room = service.getRoomByName(tokens[1]);
                if(!checkRoomExists(room))break;
                service.sendMessage(room, userService.getUser(), tokens[2]);
                break;
            // ADD_TO [room] [username]
            case "ADD_TO":
                if(!checkLoggedIn()) break;
                room = service.getRoomByName(tokens[1]);
                user = service.getUserByName(tokens[2]);
                if (!checkRoomIsGroup(room)) break;
                if (!checkUserExists(user)) break;
                userService.addUserToGroup((GroupChat)room, user);
                break;
            // KICK [room] [username]
            case "KICK":
                if(!checkLoggedIn()) break;
                room = service.getRoomByName(tokens[1]);
                user = service.getUserByName(tokens[2]);
                if (!checkRoomIsGroup(room)) break;
                if (!checkUserExists(user)) break;
                userService.kickUserFromGroup((GroupChat)room, user);
                break;
            // CREATE GROUP [name] / PRIVATE [username]
            case "CREATE":
                if(!checkLoggedIn()) break;
                if (Objects.equals(tokens[1], "GROUP")) {
                    if (service.getRoomByName(tokens[2]) != null) {
                        System.out.println("A group with this name already exists!");
                        break;
                    }
                    service.createGroupChat(tokens[2], userService.getUser());
                }
                else if (Objects.equals(tokens[1], "PRIVATE")) {
                    User otherUser = service.getUserByName(tokens[2]);
                    if(!checkUserExists(otherUser))break;
                    service.createPrivateChat(userService.getUser(), otherUser);
                }
                else {
                    System.out.println("Unknown command");
                }
                break;
            // SEARCH [room] [keyword]
            case "SEARCH":
                if(!checkLoggedIn()) break;
                room = service.getRoomByName(tokens[1]);
                String keyword = tokens[2];
                if(!checkRoomExists(room)) break;
                service.searchMessages(room, keyword);
                break;
            // MSG_STATUS [room]
            case "MSG_STATUS":
                if(!checkLoggedIn()) break;
                room = service.getRoomByName(tokens[1]);
                if(!checkRoomExists(room)) break;
                userService.showMessageStatus(room);
                break;
            // READ [room]
            case "READ":
                if(!checkLoggedIn()) break;
                room = service.getRoomByName(tokens[1]);
                if(!checkRoomExists(room)) break;
                userService.simulateReading(room);
                break;
            // UNREAD [room]
            case "UNREAD":
                if(!checkLoggedIn()) break;
                room = service.getRoomByName(tokens[1]);
                if(!checkRoomExists(room)) break;
                userService.showUnreadMessages(room);
                break;
            // UNREAD_CNT [room]
            case "UNREAD_CNT":
                if(!checkLoggedIn()) break;
                room = service.getRoomByName(tokens[1]);
                if(!checkRoomExists(room)) break;
                System.out.println(userService.getUnreadMessageCount(room));
                break;
            // ADMIN [room] [username]
            case "ADMIN":
                if(!checkLoggedIn()) break;
                room = service.getRoomByName(tokens[1]);
                user = service.getUserByName(tokens[2]);
                if (!checkRoomIsGroup(room)) break;
                if (!checkUserExists(user)) break;
                userService.makeUserAdmin((GroupChat) room, user);
                System.out.println("Made user " + user + " admin");
                break;
            // REM_ADMIN [room] [username]
            case "REM_ADMIN":
                if(!checkLoggedIn()) break;
                room = service.getRoomByName(tokens[1]);
                user = service.getUserByName(tokens[2]);
                if (!checkRoomIsGroup(room)) break;
                if (!checkUserExists(user)) break;
                userService.removeUserAdmin((GroupChat) room, user);
                System.out.println("Removed users " + user + " admin role");
                break;
            // ROLES [room]
            case "ROLES":
                room = service.getRoomByName(tokens[1]);
                if (!checkRoomIsGroup(room)) break;
                ((GroupChat) room).showPermissions();
                break;
            default:
                System.out.println("Unknown command");
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

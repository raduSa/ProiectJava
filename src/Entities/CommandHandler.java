package Entities;

import Services.ChatService;
import Services.UserService;

import java.util.Objects;

public class CommandHandler {
    private String[] tokens;
    private String input;
    ChatService service;
    UserService userService = null;

    public CommandHandler(ChatService chatService) {
        service = chatService;
    }

    public void handleCommand() {
        User user;
        ChatRoom room;

        switch(tokens[0]) {
            // SHOW_ALL USERS/ROOMS
            case "SHOW_ALL":
                if (Objects.equals(tokens[1], "USERS")) {
                    service.getUsers().values().forEach(System.out::println);
                }
                else if (Objects.equals(tokens[1], "ROOMS")) {
                    service.getChatRooms().forEach(System.out::println);
                }
                break;
            // REGISTER [username]
            case "REGISTER":
                user = service.registerUser(tokens[1]);
                System.out.printf("Registered user: %s", user);
                break;
            // LOGIN [username]
            case "LOGIN":
                userService = new UserService(service.getUsers().get(tokens[1]));
                userService.login();
                System.out.printf("Logged in as user: %s", userService.getUser());
                break;
            // LOGOUT
            case "LOGOUT":
                if(!checkLoggedIn()) break;
                userService.logout();
                userService = null;
                System.out.println("Logged out");
                break;
            // SHOW ROOMS / MSG [room] / PARTICIPANTS [room]
            case "SHOW":
                if(!checkLoggedIn()) break;
                if (Objects.equals(tokens[1], "ROOMS")) {
                    System.out.println("User is part of the following rooms:");
                    final UserService finalUserService = userService;
                    service.getChatRooms().stream().filter(chatRoom -> chatRoom.getParticipants().
                            contains(finalUserService.getUser())).forEach(System.out::println);
                }
                else if (Objects.equals(tokens[1], "MSG")) {
                    System.out.printf("Messages from room %s: \n", tokens[2]);
                    service.getChatHistory(service.getRoomByName(tokens[2])).forEach(System.out::println);
                }
                else if (Objects.equals(tokens[1], "PARTICIPANTS")) {
                    System.out.printf("Participants of room %s: \n", tokens[2]);
                    service.getChatParticipants(service.getRoomByName(tokens[2])).forEach(System.out::println);
                }
                break;
            // SEND [room] [msg]
            case "SEND":
                if(!checkLoggedIn()) break;
                service.sendMessage(service.getRoomByName(tokens[1]), userService.getUser(), tokens[2]);
                break;
            // ADD_TO [room] [username]
            case "ADD_TO":
                if(!checkLoggedIn()) break;
                room = service.getRoomByName(tokens[1]);
                user = service.getUserByName(tokens[2]);
                if (!(room instanceof GroupChat)) {
                    System.out.println("Room given is not a group chat!");
                    break;
                }
                if (user == null) {
                    System.out.println("User given does not exist!");
                    break;
                }
                userService.addUserToGroup((GroupChat)room, user);
                System.out.println("Added user " + user);
                break;
            // KICK [room] [username]
            case "KICK":
                if(!checkLoggedIn()) break;
                room = service.getRoomByName(tokens[1]);
                user = service.getUserByName(tokens[2]);
                if (!(room instanceof GroupChat)) {
                    System.out.println("Room given is not a group chat!");
                    break;
                }
                if (user == null) {
                    System.out.println("User given does not exist!");
                    break;
                }
                userService.kickUserFromGroup((GroupChat)room, user);
                System.out.println("Kicked user " + user);
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
                    if (otherUser == null) {
                        System.out.println("User does not exist!");
                        break;
                    }
                    service.createPrivateChat(userService.getUser(), otherUser);
                }
                break;
            // SEARCH [room] [keyword]
            case "SEARCH":
                if(!checkLoggedIn()) break;
                room = service.getRoomByName(tokens[1]);
                String keyword = tokens[2];
                if (room == null) {
                    System.out.println("Room does not exist");
                    break;
                }
                service.searchMessages(room, keyword);
                break;
            // MSG_STATUS [room]
            case "MSG_STATUS":
                if(!checkLoggedIn()) break;
                room = service.getRoomByName(tokens[1]);
                if (room == null) {
                    System.out.println("Room does not exist");
                    break;
                }
                userService.showMessageStatus(room);
                break;
            // READ [room]
            case "READ":
                if(!checkLoggedIn()) break;
                room = service.getRoomByName(tokens[1]);
                if (room == null) {
                    System.out.println("Room does not exist");
                    break;
                }
                userService.simulateReading(room);
                break;
            // UNREAD [room]
            case "UNREAD":
                if(!checkLoggedIn()) break;
                room = service.getRoomByName(tokens[1]);
                if (room == null) {
                    System.out.println("Room does not exist");
                    break;
                }
                userService.showUnreadMessages(room);
                break;
            // UNREAD_CNT [room]
            case "UNREAD_CNT":
                if(!checkLoggedIn()) break;
                room = service.getRoomByName(tokens[1]);
                if (room == null) {
                    System.out.println("Room does not exist");
                    break;
                }
                System.out.println(userService.getUnreadMessageCount(room));
                break;
            // ADMIN [room] [username]
            case "ADMIN":
                if(!checkLoggedIn()) break;
                room = service.getRoomByName(tokens[1]);
                user = service.getUserByName(tokens[2]);
                if (!(room instanceof GroupChat)) {
                    System.out.println("Room given is not a group chat!");
                    break;
                }
                if (user == null) {
                    System.out.println("User given does not exist!");
                    break;
                }
                userService.makeUserAdmin((GroupChat) room, user);
                System.out.println("Made user " + user + " admin");
                break;
            // REM_ADMIN [room] [username]
            case "REM_ADMIN":
                if(!checkLoggedIn()) break;
                room = service.getRoomByName(tokens[1]);
                user = service.getUserByName(tokens[2]);
                if (!(room instanceof GroupChat)) {
                    System.out.println("Room given is not a group chat!");
                    break;
                }
                if (user == null) {
                    System.out.println("User given does not exist!");
                    break;
                }
                userService.removeUserAdmin((GroupChat) room, user);
                System.out.println("Removed users " + user + " admin role");
                break;
            // ROLES [room]
            case "ROLES":
                room = service.getRoomByName(tokens[1]);
                if (!(room instanceof GroupChat)) {
                    System.out.println("Room given is not a group chat!");
                    break;
                }
                ((GroupChat) room).showPermissions();
                break;
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
}

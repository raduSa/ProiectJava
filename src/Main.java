import Entities.ChatRoom;
import Entities.GroupChat;
import Entities.PrivateChat;
import Entities.User;
import Services.ChatService;
import Services.UserService;

import java.util.Objects;
import java.util.Scanner;

public class Main {
    static ChatService service = new ChatService();
    static UserService userService = null;

    public static void main(String[] args) {
        initialise();

        Scanner scanner = new Scanner(System.in);

        // COMMANDS:
        // SHOW_ALL USERS / ROOMS
        // REGISTER [username]
        // LOGIN [username]
        // LOGOUT [username]
        // SHOW ROOMS / MSG [room] / PARTICIPANTS [room] -> for user currently logged in
        // SEND [room] [msg] -> send message
        // ADD_TO [room] [username] -> add to group
        // KICK [room] [username] -> kick from group
        // CREATE GROUP [name] / PRIVATE [username]
        // SEARCH [room] [keyword] -> search for keyword in room messages
        // MSG_STATUS [room] -> show status of room messages
        // READ [room] -> read all messages from the room
        // UNREAD [room] -> show all unread messages from the room
        // UNREAD_CNT [room] -> show number of unread messages
        // ADMIN [room] [username] -> make user admin
        // REM_ADMIN -> remove admin role from user
        // ROLES [room] -> show roles of all participants in a room

        while (true) {
            System.out.println("\nWhat is thy will:");
            String input = scanner.nextLine();
            String[] tokens = input.split(" ", 3);

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
    }

    private static void initialise() {


        User user1 = service.registerUser("user1");
        User user2 = service.registerUser("user2");
        User user3 = service.registerUser("user3");


        // Private chat

        PrivateChat privateChat = service.createPrivateChat(user1, user2);

        UserService userService1 = new UserService(user1);
        UserService userService2 = new UserService(user2);

        // Users Login
        userService1.login();
        userService2.login();

        // Send Messages
        service.sendMessage(privateChat, user1, "Hi");
        service.sendMessage(privateChat, user2, "Hey");

        // Simulate user1 reading messages
        userService1.simulateReading(privateChat);

        // Show statuses
        userService1.showMessageStatus(privateChat);
        userService2.showMessageStatus(privateChat);

        // Users logout
        userService1.logout();
        userService2.logout();

        System.out.println("Private Chat History:");
        service.getChatHistory(privateChat).forEach(System.out::println);


        // Group chat

        GroupChat groupChat = service.createGroupChat("Group", user1);
        userService1.addUserToGroup(groupChat, user2);
        userService1.addUserToGroup(groupChat, user3);

        service.sendMessage(groupChat, user3, "Hello everyone!");
        service.sendMessage(groupChat, user2, "Hey there");
        service.sendMessage(groupChat, user1, "Whats up");
        service.sendMessage(groupChat, user2, "Epic");
        service.sendMessage(groupChat, user3, "I love pancakes");

        System.out.println("\nGroup Chat Members:");
        service.getChatParticipants(groupChat).forEach(System.out::println);

        System.out.println("\nGroup Chat History:");
        service.getChatHistory(groupChat).forEach(System.out::println);

        System.out.println("\nSearch for 'hello' in Group Chat:");
        service.searchMessages(groupChat, "hello");
    }

    private static Boolean checkLoggedIn() {
        if (userService == null) {
            System.out.println("Not logged in!");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}

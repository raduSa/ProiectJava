import Entities.*;
import Services.ChatService;
import Services.SessionService;
import Services.UserService;

import java.util.Scanner;

public class Main {
    public static ChatService service = new ChatService();
    public static SessionService sessionService = new SessionService();

    public static void main(String[] args) {
        initialise();

        Scanner scanner = new Scanner(System.in);
        CommandHandler handler = new CommandHandler(service, sessionService);

        // can add a Command class to handle command

        // COMMANDS:
        // SHOW_ALL USERS / ROOMS / ACTIVE_SESSIONS / INACTIVE_SESSIONS
        // REGISTER [username]
        // LOGIN [username]
        // LOGOUT [username]
        // SHOW ROOMS / MSG [room] / PARTICIPANTS [room] / EMPTY_SLOTS [room] -> for user currently logged in
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
            System.out.println("\nEnter command:");
            String input = scanner.nextLine();

            handler.readCommand(input);
            handler.handleCommand();
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
        sessionService.login(user1);
        userService2.login();
        sessionService.login(user2);

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
        sessionService.logout(user1);
        userService2.logout();
        sessionService.logout(user2);

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

        System.out.println("\nGroup Chat Members (Alphabetically sorted):");
        service.getChatParticipants(groupChat).forEach(System.out::println);

        System.out.println("\nGroup Chat History:");
        service.getChatHistory(groupChat).forEach(System.out::println);

        System.out.println("\nSearch for 'hello' in Group Chat:");
        service.searchMessages(groupChat, "hello");
    }
}

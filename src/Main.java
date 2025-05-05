import Entities.GroupChat;
import Entities.PrivateChat;
import Entities.User;
import Services.ChatService;
import Services.UserService;

public class Main {
    public static void main(String[] args) {
        ChatService service = new ChatService();

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

        // Simulate typing
        service.getTypingNotifier().startTyping(privateChat, user1);
        service.getTypingNotifier().startTyping(privateChat, user2);
        service.getTypingNotifier().showTyping(privateChat);
        service.getTypingNotifier().stopTyping(privateChat, user1);
        service.getTypingNotifier().stopTyping(privateChat, user2);

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
        service.addUserToGroup(groupChat, user2);
        service.addUserToGroup(groupChat, user3);

        service.sendMessage(groupChat, user3, "Hello everyone!");
        service.sendMessage(groupChat, user2, "Hey there");

        System.out.println("\nGroup Chat Members:");
        service.getChatParticipants(groupChat).forEach(System.out::println);

        System.out.println("\nGroup Chat History:");
        service.getChatHistory(groupChat).forEach(System.out::println);

        System.out.println("\nSearch for 'hello' in Group Chat:");
        service.searchMessages(groupChat, "hello");
    }
}

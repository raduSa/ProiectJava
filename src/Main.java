import Entities.GroupChat;
import Entities.PrivateChat;
import Entities.User;
import Services.ChatService;

public class Main {
    public static void main(String[] args) {
        ChatService service = new ChatService();

        User user1 = service.registerUser("user1");
        User user2 = service.registerUser("user2");
        User user3 = service.registerUser("user3");

        PrivateChat privateChat = service.createPrivateChat(user1, user2);
        service.sendMessage(privateChat, user1, "Hi");
        service.sendMessage(privateChat, user2, "Hey");

        System.out.println("Private Chat History:");
        service.getChatHistory(privateChat).forEach(System.out::println);

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

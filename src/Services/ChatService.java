package Services;

import Entities.*;

import java.util.*;

public class ChatService {
    private Map<String, User> users = new HashMap<>();
    private List<ChatRoom> chatRooms = new ArrayList<>();

    public User registerUser(String username) {
        if (users.containsKey(username)) return users.get(username);
        User user = new User(username);
        users.put(username, user);
        return user;
    }

    public PrivateChat createPrivateChat(User u1, User u2) {
        PrivateChat chat = new PrivateChat(u1, u2);
        chatRooms.add(chat);
        return chat;
    }

    public GroupChat createGroupChat(String name, User creator) {
        GroupChat group = new GroupChat(name, creator);
        chatRooms.add(group);
        return group;
    }

    public void sendMessage(ChatRoom room, User sender, String content) {
        if (room.getParticipants().contains(sender)) {
            room.sendMessage(new Message(content, sender));
        }
    }

    public void addUserToGroup(GroupChat group, User user) {
        if (group.canAdd(user)) {
            group.addParticipant(user);
        }
    }

    public List<Message> getChatHistory(ChatRoom room) {
        return room.getMessages();
    }

    public Set<User> getChatParticipants(ChatRoom room) {
        return room.getParticipants();
    }

    public void searchMessages(ChatRoom room, String keyword) {
        room.getMessages().stream()
                .filter(msg -> msg.getContent().toLowerCase().contains(keyword.toLowerCase()))
                .forEach(System.out::println);
    }
}


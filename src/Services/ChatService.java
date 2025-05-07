package Services;

import Entities.*;
import Utils.GroupPermission;

import java.util.*;

public class ChatService {
    private Map<String, User> users = new HashMap<>();
    private List<ChatRoom> chatRooms = new ArrayList<>();

    public User registerUser(String username) {
        if (users.containsKey(username)) {
            System.out.println("User " + username + " already exists!");
            return users.get(username);
        }
        System.out.printf("Registered user: %s", username);
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
        group.setPermissions(creator, GroupPermission.OWNER);
        chatRooms.add(group);
        return group;
    }

    public void sendMessage(ChatRoom room, User sender, String content) {
        if (room.getParticipants().contains(sender)) {
            Message msg = new Message(content, sender);
            msg.initializeStatus(room.getParticipants());
            room.sendMessage(msg);
            System.out.println("Message sent");
            return;
        }
        System.out.println("User is not part of this group!");
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

    public ChatRoom getRoomByName(String roomName) {
        for (ChatRoom room : chatRooms) {
            if (Objects.equals(room.getName(), roomName)) {
                return room;
            }
        }
        return null;
    }

    public User getUserByName(String username) {
        return users.getOrDefault(username, null);
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public List<ChatRoom> getChatRooms() {
        return chatRooms;
    }

    public void showEmptySlots(ChatRoom room) {
        System.out.println("Number of empty slots: " + room.emptySlots());
    }
}


package Services;

import Entities.*;
import Repository.ChatRoomJdbcService;
import Repository.MessageJdbcService;
import Repository.UserJdbcService;
import Utils.GroupPermission;
import Utils.MessageStatus;

import java.util.*;

public class ChatService {
    private Map<String, User> users = new HashMap<>();
    private List<ChatRoom> chatRooms = new ArrayList<>();

    public User registerUser(String username) {
        User newUser = UserJdbcService.getInstance().createUser(username);
        if (newUser != null) {
            System.out.printf("Registered user: %s\n", username);
        }
        return newUser;
    }

    public PrivateChat createPrivateChat(User u1, User u2) {
        PrivateChat chat = new PrivateChat(u1, u2);
        ChatRoomJdbcService.getInstance().createChatRoom(chat, "PRIVATE", 2);
        System.out.println("Created private chat: " + chat.getName());
        return chat;
    }

    public GroupChat createGroupChat(String name, User creator) {
        GroupChat group = new GroupChat(name, creator);
        group.setPermissions(creator, GroupPermission.OWNER);
        ChatRoomJdbcService.getInstance().createChatRoom(group, "GROUP", 50);
        System.out.println("Created group chat: " + name);
        return group;
    }

    public void sendMessage(ChatRoom room, User sender, String content) {
        Set<User> participants = ChatRoomJdbcService.getInstance().getParticipants(room.getId());
        // check that the sender is part of the group
        if (participants.stream().anyMatch(participant -> Objects.equals(participant.getUsername(), sender.getUsername()))) {
            // create the message
            Message msg = new Message(content, sender);
            int message_id = MessageJdbcService.getInstance().createMessage(msg, room.getId());
            // initialize and save the delivery statuses
            msg.initializeStatus(participants);
            MessageJdbcService.getInstance().addMessageDeliveryStatus(message_id, msg);

            System.out.println("Message sent");
            return;
        }
        System.out.println("User is not part of this group!");
    }

    public List<Message> getChatHistory(int room_id) {
        List<Message> messages = MessageJdbcService.getInstance().getMessagesByChatRoomId(room_id);
        if (messages.isEmpty()) {
            System.out.println("No messages!");
        }
        else {
            messages.forEach(System.out::println);
        }
        return messages;
    }

    public Set<User> getChatParticipants(int room_id) {
        Set<User> participants = ChatRoomJdbcService.getInstance().getParticipants(room_id);
        if (participants.isEmpty()) {
            System.out.println("No messages!");
        }
        else {
            participants.forEach(System.out::println);
        }
        return participants;
    }

    public void searchMessages(ChatRoom room, String keyword) {
        List<Message> messages = MessageJdbcService.getInstance().getMessagesByChatRoomId(room.getId());
        messages.stream()
                .filter(msg -> msg.getContent().toLowerCase().contains(keyword.toLowerCase()))
                .forEach(System.out::println);
    }

    public ChatRoom getRoomById(Integer roomId) {
        return ChatRoomJdbcService.getInstance().getChatRoomById(roomId);
    }

    public User getUserByName(String username) {
        return UserJdbcService.getInstance().getUserByUsername(username);
    }

    public List<User> getUsers() {
        return UserJdbcService.getInstance().getAllUsers();
    }

    public void showEmptySlots(int room_id) {
        System.out.println("Number of empty slots: " + ChatRoomJdbcService.getInstance().getChatRoomById(room_id).emptySlots());
    }

    public List<String> getChatRoomNames() {
        return ChatRoomJdbcService.getInstance().getAllChatRooms();
    }

    public void getChatRoomsWithMember(User user) {
        List<String> chatRooms = ChatRoomJdbcService.getInstance().getChatRoomsWithMember(user.getUsername());
        if (chatRooms.isEmpty()) {
            System.out.println("Not a part of any rooms!");
        }
        else {
            chatRooms.forEach(System.out::println);
        }
    }

    public void showRoomPermissions(ChatRoom room) {

    }
}


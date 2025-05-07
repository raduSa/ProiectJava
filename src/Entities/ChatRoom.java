package Entities;

import java.util.*;

public abstract class ChatRoom {
    protected String name;
    protected List<Message> messages = new ArrayList<>();
    protected Set<User> participants = new TreeSet<>();

    public ChatRoom(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public List<Message> getMessages() { return messages; }
    public Set<User> getParticipants() { return participants; }

    public void sendMessage(Message msg) {
        messages.add(msg);
    }

    public void addParticipant(User user) {
        if (participants.contains(user)) {
            System.out.println("User is already part of this room!");
            return;
        }
        participants.add(user);
        System.out.println("Added user " + user);
    }

    public void removeParticipant(User user) {
        if (!participants.contains(user)) {
            System.out.println("User is not part of this room!");
            return;
        }
        participants.remove(user);
        System.out.println("Kicked user " + user);
    }

    public abstract Integer emptySlots();

    @Override
    public String toString() {
        return name;
    }
}

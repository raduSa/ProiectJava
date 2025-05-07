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
        participants.add(user);
    }

    public void removeParticipant(User user) {
        participants.remove(user);
    }

    public abstract Integer emptySlots();

    @Override
    public String toString() {
        return name;
    }
}

package Entities;

public class GroupChat extends ChatRoom {
    public GroupChat(String name, User creator) {
        super(name);
        addParticipant(creator);
    }

    @Override
    public boolean canAdd(User user) {
        return true;
    }
}


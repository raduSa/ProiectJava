package Entities;

public class PrivateChat extends ChatRoom {
    public PrivateChat(User user1, User user2) {
        super(user1.getUsername() + "-" + user2.getUsername());
        addParticipant(user1);
        addParticipant(user2);
    }

    @Override
    public boolean canAdd(User user) {
        return participants.size() < 2;
    }
}

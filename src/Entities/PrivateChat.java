package Entities;

public class PrivateChat extends ChatRoom {
    public PrivateChat(User user1, User user2) {
        super("DM: " + user1.getUsername() + "-" + user2.getUsername());
        addParticipant(user1);
        addParticipant(user2);
    }

    public PrivateChat(User user1, User user2, int roomId) {
        super("DM: " + user1.getUsername() + "-" + user2.getUsername(), roomId);
        addParticipant(user1);
        addParticipant(user2);
    }


    @Override
    public Integer emptySlots() {
        return 0;
    }
}

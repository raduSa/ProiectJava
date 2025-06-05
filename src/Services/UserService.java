package Services;

import Entities.ChatRoom;
import Entities.GroupChat;
import Entities.Message;
import Entities.User;
import Repository.ChatRoomJdbcService;
import Repository.MessageJdbcService;
import Repository.UserJdbcService;
import Utils.GroupPermission;
import Utils.MessageStatus;
import Utils.UserStatus;

import java.util.List;

public class UserService {
    private User user;

    public UserService(User user) {
        this.user = user;
    }

    public void login() {
        user.setStatus(UserStatus.ONLINE);
        UserJdbcService.getInstance().updateUser(user);
        System.out.println(user.getUsername() + " logged in.");
    }

    public void logout() {
        user.setStatus(UserStatus.OFFLINE);
        UserJdbcService.getInstance().updateUser(user);
        System.out.println(user.getUsername() + " logged out.");
    }

    public void simulateReading(ChatRoom room) {
        for (Message msg : MessageJdbcService.getInstance().getMessagesByChatRoomId(room.getId())) {
            if (!msg.getSender().getUsername().equals(user.getUsername())) {
                MessageJdbcService.getInstance().updateMessageStatus(msg.getId(), user.getUsername(), MessageStatus.READ);
            }
        }
        System.out.println(user.getUsername() + " read all messages in " + room.getName());
    }

    public void showMessageStatus(ChatRoom room) {
        System.out.println("Message Status for " + user.getUsername() + " in chat " + room.getName() + ":");
        for (Message msg : MessageJdbcService.getInstance().getMessagesByChatRoomId(room.getId())) {
            if (msg.getStatus(user.getUsername()) != null) {
                System.out.println(msg + " [Status: " + msg.getStatus(user.getUsername()) + "]");
            }
        }
    }

    public void showUnreadMessages(ChatRoom room) {
        System.out.println("Unread messages for " + user.getUsername() + " in chat " + room.getName() + ":");
        List<Message> messages = MessageJdbcService.getInstance().getMessagesByChatRoomId(room.getId());
        boolean hasUnread = false;
        for (Message msg : messages) {
            if (msg.getStatus(user.getUsername()) == MessageStatus.RECEIVED) {
                System.out.println(msg);
                hasUnread = true;
            }
        }
        if (!hasUnread) {
            System.out.println("No unread messages.");
        }
    }

    public int getUnreadMessageCount(ChatRoom room) {
        System.out.println("Number of unread messages for " + user.getUsername() + " in chat " + room.getName() + ":");
        List<Message> messages = MessageJdbcService.getInstance().getMessagesByChatRoomId(room.getId());
        int count = 0;
        for (Message msg : messages) {
            if (msg.getStatus(user.getUsername()) == MessageStatus.RECEIVED) {
                count++;
            }
        }
        return count;
    }

    public void addUserToGroup(GroupChat group, User otherUser) {
        if (ChatRoomJdbcService.getInstance().getParticipantPermission(group.getId(), user.getUsername()) == GroupPermission.MEMBER) {
            System.out.println("Only the owner and admins can add new members!");
            return;
        }
        group.addParticipant(otherUser);
        ChatRoomJdbcService.getInstance().addParticipant(group.getId(), otherUser.getUsername(), GroupPermission.MEMBER);
        System.out.println("Added new member " + otherUser);
    }

    public void kickUserFromGroup(GroupChat group, User otherUser) {
        if (ChatRoomJdbcService.getInstance().getParticipantPermission(group.getId(), user.getUsername()) == GroupPermission.MEMBER) {
            System.out.println("Only the owner and admins can kick members!");
            return;
        }
        if (ChatRoomJdbcService.getInstance().getParticipantPermission(group.getId(), otherUser.getUsername()) == GroupPermission.OWNER) {
            System.out.println("Cannot kick the owner!");
            return;
        }
        group.removeParticipant(otherUser);
        ChatRoomJdbcService.getInstance().removeParticipant(group.getId(), otherUser.getUsername());
    }

    public void makeUserAdmin(GroupChat group, User otherUser) {
        if (ChatRoomJdbcService.getInstance().getParticipantPermission(group.getId(), user.getUsername()) != GroupPermission.OWNER) {
            System.out.println("Only the owner can make users admins!");
            return;
        }
        ChatRoomJdbcService.getInstance().updateParticipantPermission(group.getId(), otherUser.getUsername(), GroupPermission.ADMIN);
        System.out.println("Made user " + otherUser + " an admin");
    }

    public void removeUserAdmin(GroupChat group, User otherUser) {
        if (ChatRoomJdbcService.getInstance().getParticipantPermission(group.getId(), user.getUsername()) != GroupPermission.OWNER) {
            System.out.println("Only the owner can take away roles!");
            return;
        }
        ChatRoomJdbcService.getInstance().updateParticipantPermission(group.getId(), otherUser.getUsername(), GroupPermission.MEMBER);
        System.out.println("Removed user's " + user + " admin role");
    }

    public User getUser() {
        return user;
    }
}
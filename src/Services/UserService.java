package Services;

import Entities.ChatRoom;
import Entities.GroupChat;
import Entities.Message;
import Entities.User;
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
        System.out.println(user.getUsername() + " logged in.");
    }

    public void logout() {
        user.setStatus(UserStatus.OFFLINE);
        System.out.println(user.getUsername() + " logged out.");
    }

    public void simulateReading(ChatRoom room) {
        for (Message msg : room.getMessages()) {
            if (!msg.getSender().equals(user)) {
                msg.markRead(user);
            }
        }
        System.out.println(user.getUsername() + " read all messages in " + room.getName());
    }

    public void showMessageStatus(ChatRoom room) {
        System.out.println("Message Status for " + user.getUsername() + " in chat " + room.getName() + ":");
        for (Message msg : room.getMessages()) {
            System.out.println(msg + " [Status: " + msg.getStatus(user) + "]");
        }
    }

    public void showUnreadMessages(ChatRoom room) {
        System.out.println("Unread messages for " + user.getUsername() + " in chat " + room.getName() + ":");
        List<Message> messages = room.getMessages();
        boolean hasUnread = false;
        for (Message msg : messages) {
            if (!msg.getSender().equals(user) && msg.getStatus(user) != MessageStatus.READ) {
                System.out.println(msg);
                hasUnread = true;
            }
        }
        if (!hasUnread) {
            System.out.println("No unread messages.");
        }
    }

    public int getUnreadMessageCount(ChatRoom room) {
        int count = 0;
        for (Message msg : room.getMessages()) {
            if (!msg.getSender().equals(user) && msg.getStatus(user) != MessageStatus.READ) {
                count++;
            }
        }
        return count;
    }

    public void addUserToGroup(GroupChat group, User otherUser) {
        if (group.getPermission(user) == GroupPermission.MEMBER) {
            System.out.println("Only the owner and admins can add new members!");
            return;
        }
        group.addParticipant(otherUser);
    }

    public void kickUserFromGroup(GroupChat group, User otherUser) {
        if (group.getPermission(user) == GroupPermission.MEMBER) {
            System.out.println("Only the owner and admins can kick users!");
            return;
        }
        if (group.getPermission(otherUser) == GroupPermission.OWNER) {
            System.out.println("Cannot kick the owner!");
            return;
        }
        group.removeParticipant(otherUser);
    }

    public void makeUserAdmin(GroupChat group, User otherUser) {
        if (group.getPermission(user) != GroupPermission.OWNER) {
            System.out.println("Only the owner can make users admins!");
            return;
        }
        group.setPermissions(otherUser, GroupPermission.ADMIN);
    }

    public void removeUserAdmin(GroupChat group, User otherUser) {
        if (group.getPermission(user) != GroupPermission.OWNER) {
            System.out.println("Only the owner can take away roles!");
            return;
        }
        group.setPermissions(otherUser, GroupPermission.MEMBER);
    }

    public void showUserStatus(User user) {
        System.out.println("User " + user.getUsername() + " is currently " + user.getStatus());
    }

    public User getUser() {
        return user;
    }
}
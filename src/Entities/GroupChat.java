package Entities;

import Utils.GroupPermission;

import java.util.HashMap;
import java.util.Map;

public class GroupChat extends ChatRoom {
    private Map<User, GroupPermission> permissions = new HashMap<>();

    public GroupChat(String name, User creator) {
        super(name);
        addParticipant(creator);
    }

    @Override
    public boolean canAdd(User user) {
        return true;
    }

    @Override
    public void addParticipant(User user) {
        super.addParticipant(user);
        permissions.put(user, GroupPermission.MEMBER);
    }

    public void setPermissions(User user, GroupPermission permission) {
        permissions.put(user, permission);
    }

    public GroupPermission getPermission(User user) {
        return permissions.get(user);
    }

    public void showPermissions() {
        permissions.forEach((key, value) -> System.out.println("User: " + key + ", Role: " + value));
    }
}


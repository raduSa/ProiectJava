package Entities;

import Utils.GroupPermission;

import java.util.HashMap;
import java.util.Map;

public class GroupChat extends ChatRoom {
    private Map<String, GroupPermission> permissions = new HashMap<>();
    private final Integer maxUsers = 50;

    public GroupChat(String name, User creator) {
        super(name);
        addParticipant(creator);
        setPermissions(creator, GroupPermission.OWNER);
    }

    public GroupChat(String name, User creator, int roomId) {
        super(name, roomId);
        addParticipant(creator);
        setPermissions(creator, GroupPermission.OWNER);
    }


    @Override
    public Integer emptySlots() {
        return maxUsers - permissions.size();
    }

    @Override
    public void addParticipant(User user) {
        super.addParticipant(user);
        permissions.put(user.getUsername(), GroupPermission.MEMBER);
    }

    public void setPermissions(User user, GroupPermission permission) {
        permissions.put(user.getUsername(), permission);
    }

    public GroupPermission getPermission(User user) {
        return permissions.get(user);
    }

    public void showPermissions() {
        permissions.forEach((key, value) -> System.out.println("User: " + key + ", Role: " + value));
    }
}


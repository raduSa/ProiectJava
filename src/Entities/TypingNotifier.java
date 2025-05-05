package Entities;

import java.util.*;

public class TypingNotifier {
    private Map<ChatRoom, Set<User>> typingUsers = new HashMap<>();

    public void startTyping(ChatRoom room, User user) {
        typingUsers.computeIfAbsent(room, k -> new HashSet<>()).add(user);
    }

    public void stopTyping(ChatRoom room, User user) {
        Set<User> set = typingUsers.get(room);
        if (set != null) set.remove(user);
    }

    public void showTyping(ChatRoom room) {
        Set<User> set = typingUsers.get(room);
        if (set != null && !set.isEmpty()) {
            System.out.print("Typing: ");
            for (User u : set) {
                System.out.print(u.getUsername() + " ");
            }
            System.out.println("...");
        }
    }
}

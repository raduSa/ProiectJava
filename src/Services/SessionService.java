package Services;

import Entities.User;
import Entities.UserSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionService {
    private Map<String, List<UserSession>> inactiveSessions = new HashMap<>();
    private Map<String, UserSession> activeSessions = new HashMap<>();

    public void login(User user) {
        UserSession session = new UserSession(user);
        activeSessions.put(user.getUsername(), session);
    }

    public void logout(User user) {
        String username = user.getUsername();
        UserSession session = activeSessions.get(username);
        if (session != null) {
            session.logout();
            activeSessions.remove(username);
            inactiveSessions.computeIfAbsent(username, k -> new ArrayList<>()).add(session);
        }
    }

    public void showActiveSessions() {
        if (activeSessions.isEmpty()) {
            System.out.println("No active sessions");
            return;
        }
        activeSessions.forEach((key, value) ->
                System.out.println("User: " + key + ", Login time: " + value.getLoginTime()));
    }

    public void showInactiveSessions() {
        if (inactiveSessions.isEmpty()) {
            System.out.println("No inactive sessions");
            return;
        }
        inactiveSessions.forEach((key, value) ->
                System.out.println("User: " + key + ", " + value));
    }
}

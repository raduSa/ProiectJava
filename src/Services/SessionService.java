package Services;

import Entities.User;
import Entities.UserSession;
import Repository.UserSessionJdbcService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionService {
    private Map<String, List<UserSession>> inactiveSessions = new HashMap<>();
    private Map<String, UserSession> activeSessions = new HashMap<>();

    public void login(User user) {
        UserSession session = new UserSession(user);
        UserSessionJdbcService.getInstance().createUserSession(session);
    }

    public void logout(User user) {
        String username = user.getUsername();
        UserSessionJdbcService.getInstance().endUserSession(username);
    }

    public void showActiveSessions() {
        Map<String, UserSession> activeSessions = UserSessionJdbcService.getInstance().getActiveSessions();
        if (activeSessions.isEmpty()) {
            System.out.println("No active sessions");
            return;
        }
        activeSessions.forEach((key, value) ->
                System.out.println("User: " + key + ", Login time: " + value.getLoginTime()));
    }

    public void showInactiveSessions() {
        Map<String, List<UserSession>> inactiveSessions = UserSessionJdbcService.getInstance().getInactiveSessions();
        if (inactiveSessions.isEmpty()) {
            System.out.println("No inactive sessions");
            return;
        }
        inactiveSessions.forEach((key, value) -> {
                    System.out.println("User: " + key + ": ");
                    value.forEach(System.out::println);
                }
                );
    }
}

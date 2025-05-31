package Repository;

import Config.Constants;
import Config.DatabaseConnection;
import Entities.User;
import Entities.UserSession;
import Services.AuditService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton service that handles CRUD operations for UserSession entities
 */
public class UserSessionJdbcService {
    private static UserSessionJdbcService instance;
    private final AuditService auditService;
    private final UserJdbcService userService;
    
    private UserSessionJdbcService() {
        auditService = AuditService.getInstance();
        userService = UserJdbcService.getInstance();
    }
    
    public static UserSessionJdbcService getInstance() {
        if (instance == null) {
            instance = new UserSessionJdbcService();
        }
        return instance;
    }
    
    /**
     * Create a new user session in the database
     * @param session The user session to create
     * @return The session ID of the created session, or -1 if failed
     */
    public int createUserSession(UserSession session) {
        String sql = "INSERT INTO " + Constants.USER_SESSION_TABLE + 
                     " (username, login_time, ip_address) VALUES (?, ?, ?)";
        int sessionId = -1;
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setString(1, session.getUser().getUsername());
            stmt.setTimestamp(2, Timestamp.valueOf(session.getLoginTime()));
            stmt.setString(3, getSessionIpAddress(session));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        sessionId = generatedKeys.getInt(1);
                        auditService.log("CREATE_USER_SESSION");
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating user session: " + e.getMessage());
        }
        
        return sessionId;
    }
    
    /**
     * Get the IP address from a UserSession using reflection
     * @param session The user session
     * @return The IP address
     */
    private String getSessionIpAddress(UserSession session) {
        try {
            java.lang.reflect.Field ipAddressField = UserSession.class.getDeclaredField("ipAddress");
            ipAddressField.setAccessible(true);
            return (String) ipAddressField.get(session);
        } catch (Exception e) {
            System.err.println("Error getting IP address: " + e.getMessage());
            return "0.0.0.0"; // Default fallback
        }
    }
    
    /**
     * Get a user session by its ID
     * @param sessionId The ID of the session
     * @return The UserSession if found, null otherwise
     */
    public UserSession getUserSessionById(int sessionId) {
        String sql = "SELECT * FROM " + Constants.USER_SESSION_TABLE + " WHERE session_id = ?";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, sessionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUserSession(rs);
                }
            }
            
            auditService.log("READ_USER_SESSION");
            
        } catch (SQLException e) {
            System.err.println("Error retrieving user session: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get all user sessions for a user
     * @param username The username
     * @return List of user sessions
     */
    public List<UserSession> getUserSessionsByUsername(String username) {
        String sql = "SELECT * FROM " + Constants.USER_SESSION_TABLE + " WHERE username = ?";
        List<UserSession> sessions = new ArrayList<>();
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UserSession session = mapResultSetToUserSession(rs);
                    if (session != null) {
                        sessions.add(session);
                    }
                }
            }
            
            auditService.log("READ_USER_SESSIONS_BY_USERNAME");
            
        } catch (SQLException e) {
            System.err.println("Error retrieving user sessions: " + e.getMessage());
        }
        
        return sessions;
    }
    
    /**
     * Get all active user sessions
     * @return List of active user sessions
     */
    public List<UserSession> getActiveSessions() {
        String sql = "SELECT * FROM " + Constants.USER_SESSION_TABLE + " WHERE logout_time IS NULL";
        List<UserSession> sessions = new ArrayList<>();
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                UserSession session = mapResultSetToUserSession(rs);
                if (session != null) {
                    sessions.add(session);
                }
            }
            
            auditService.log("READ_ACTIVE_SESSIONS");
            
        } catch (SQLException e) {
            System.err.println("Error retrieving active sessions: " + e.getMessage());
        }
        
        return sessions;
    }
    
    /**
     * Map a ResultSet to a UserSession object
     * @param rs The ResultSet
     * @return The mapped UserSession, or null if mapping failed
     */
    private UserSession mapResultSetToUserSession(ResultSet rs) throws SQLException {
        String username = rs.getString("username");
        LocalDateTime loginTime = rs.getTimestamp("login_time").toLocalDateTime();
        Timestamp logoutTimestamp = rs.getTimestamp("logout_time");
        LocalDateTime logoutTime = logoutTimestamp != null ? logoutTimestamp.toLocalDateTime() : null;
        String ipAddress = rs.getString("ip_address");
        int sessionId = rs.getInt("session_id");
        
        User user = userService.getUserByUsername(username);
        if (user == null) {
            user = new User(username); // Fallback if user not found
        }
        
        // Create a UserSession using reflection to set private fields
        try {
            UserSession session = new UserSession(user);
            
            // Set session ID
            java.lang.reflect.Field sessionIdField = UserSession.class.getDeclaredField("sessionId");
            sessionIdField.setAccessible(true);
            sessionIdField.set(session, sessionId);
            
            // Set login time
            java.lang.reflect.Field loginTimeField = UserSession.class.getDeclaredField("loginTime");
            loginTimeField.setAccessible(true);
            loginTimeField.set(session, loginTime);
            
            // Set IP address
            java.lang.reflect.Field ipAddressField = UserSession.class.getDeclaredField("ipAddress");
            ipAddressField.setAccessible(true);
            ipAddressField.set(session, ipAddress);
            
            // Set logout time if exists
            if (logoutTime != null) {
                java.lang.reflect.Field logoutTimeField = UserSession.class.getDeclaredField("logoutTime");
                logoutTimeField.setAccessible(true);
                logoutTimeField.set(session, logoutTime);
            }
            
            return session;
        } catch (Exception e) {
            System.err.println("Error mapping ResultSet to UserSession: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * End a user session (set logout time)
     * @param sessionId The ID of the session to end
     * @return True if successful
     */
    public boolean endUserSession(int sessionId) {
        String sql = "UPDATE " + Constants.USER_SESSION_TABLE + 
                     " SET logout_time = ? WHERE session_id = ? AND logout_time IS NULL";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, sessionId);
            
            int affectedRows = stmt.executeUpdate();
            auditService.log("END_USER_SESSION");
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error ending user session: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete a user session
     * @param sessionId The ID of the session to delete
     * @return True if successful
     */
    public boolean deleteUserSession(int sessionId) {
        String sql = "DELETE FROM " + Constants.USER_SESSION_TABLE + " WHERE session_id = ?";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setInt(1, sessionId);
            
            int affectedRows = stmt.executeUpdate();
            auditService.log("DELETE_USER_SESSION");
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting user session: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete all user sessions for a user
     * @param username The username
     * @return The number of sessions deleted
     */
    public int deleteAllUserSessions(String username) {
        String sql = "DELETE FROM " + Constants.USER_SESSION_TABLE + " WHERE username = ?";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, username);
            
            int affectedRows = stmt.executeUpdate();
            auditService.log("DELETE_ALL_USER_SESSIONS");
            return affectedRows;
            
        } catch (SQLException e) {
            System.err.println("Error deleting user sessions: " + e.getMessage());
            return 0;
        }
    }
}

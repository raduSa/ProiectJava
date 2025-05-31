package Repository;

import Config.Constants;
import Config.DatabaseConnection;
import Entities.User;
import Services.AuditService;
import Utils.UserStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton service that handles CRUD operations for User entities
 */
public class UserJdbcService {
    private static UserJdbcService instance;
    private final AuditService auditService;
    
    private UserJdbcService() {
        auditService = AuditService.getInstance();
    }
    
    public static UserJdbcService getInstance() {
        if (instance == null) {
            instance = new UserJdbcService();
        }
        return instance;
    }
    
    /**
     * Create a new user in the database
     * @param user The user to create
     * @return True if the operation was successful
     */
    public boolean createUser(User user) {
        String sql = "INSERT INTO " + Constants.USER_TABLE + " (username, status) VALUES (?, ?)";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getStatus().toString());
            
            int affectedRows = stmt.executeUpdate();
            auditService.log("CREATE_USER");
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Retrieve a user from the database by username
     * @param username The username to search for
     * @return The User if found, null otherwise
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM " + Constants.USER_TABLE + " WHERE username = ?";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(rs.getString("username"));
                    user.setStatus(UserStatus.valueOf(rs.getString("status")));
                    auditService.log("READ_USER");
                    return user;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving user: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Retrieve all users from the database
     * @return List of all users
     */
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM " + Constants.USER_TABLE;
        List<User> users = new ArrayList<>();
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                User user = new User(rs.getString("username"));
                user.setStatus(UserStatus.valueOf(rs.getString("status")));
                users.add(user);
            }
            
            auditService.log("READ_ALL_USERS");
            
        } catch (SQLException e) {
            System.err.println("Error retrieving all users: " + e.getMessage());
        }
        
        return users;
    }
    
    /**
     * Update a user's status in the database
     * @param user The user with updated information
     * @return True if the operation was successful
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE " + Constants.USER_TABLE + " SET status = ? WHERE username = ?";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, user.getStatus().toString());
            stmt.setString(2, user.getUsername());
            
            int affectedRows = stmt.executeUpdate();
            auditService.log("UPDATE_USER");
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete a user from the database
     * @param username The username of the user to delete
     * @return True if the operation was successful
     */
    public boolean deleteUser(String username) {
        String sql = "DELETE FROM " + Constants.USER_TABLE + " WHERE username = ?";
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, username);
            
            int affectedRows = stmt.executeUpdate();
            auditService.log("DELETE_USER");
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }
}

package Repository;

import Config.Constants;
import Config.DatabaseConnection;
import Entities.User;
import Services.AuditService;
import Utils.UserStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserJdbcService {
    private static UserJdbcService instance;
    
    private UserJdbcService() {
    }
    
    public static UserJdbcService getInstance() {
        if (instance == null) {
            instance = new UserJdbcService();
        }
        return instance;
    }
    
    /**
     * Create a new user in the database
     * @param username The username to use
     * @return True if the operation was successful
     */
    public User createUser(String username) {
        String sql = "INSERT INTO " + Constants.USER_TABLE + " (username, status) VALUES (?, ?)";

        // First check if user already exists
        if (getUserByUsername(username) != null) {
            System.out.println("User " + username + " already exists!");
            return null;
        }

        User newUser = new User(username);
        
        try {
            Connection conn = DatabaseConnection.getDatabaseConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, newUser.getUsername());
            stmt.setString(2, newUser.getStatus().toString());
            
            int affectedRows = stmt.executeUpdate();
            return newUser;
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            return null;
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
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }
}

package Services;

import Config.Constants;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Singleton service that logs all CRUD operations to a CSV file
 */
public class AuditService {
    private static AuditService instance;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private AuditService() {
        // Private constructor to enforce singleton pattern
    }
    
    public static AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }
    
    /**
     * Logs an action to the audit CSV file
     * @param actionName The name of the action performed (e.g., "CREATE_USER")
     */
    public void log(String actionName) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(Constants.AUDIT_FILE, true))) {
            String timestamp = LocalDateTime.now().format(formatter);
            writer.println(actionName + "," + timestamp);
        } catch (IOException e) {
            System.err.println("Error writing to audit file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

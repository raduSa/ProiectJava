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
     * Logs a command and its description to the audit CSV file.
     * @param command The command executed (e.g., "REGISTER", "LOGIN").
     * @param description A description of the command's parameters or context (e.g., "user: john_doe").
     */
    public void log(String command, String description) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(Constants.AUDIT_FILE, true))) {
            String timestamp = LocalDateTime.now().format(formatter);
            String logEntry = command;
            if (description != null && !description.isEmpty()) {
                logEntry += " (" + description + ")";
            }
            writer.println(logEntry + "," + timestamp);
        } catch (IOException e) {
            System.err.println("Error writing to audit file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

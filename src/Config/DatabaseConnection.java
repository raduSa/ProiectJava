package Config;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static Utils.FilesUtils.citireProprietati;

public class DatabaseConnection {
    private static Connection databaseConnection = null;

    private final static String dbUrl, username, pass;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            dbUrl = citireProprietati(Constants.FISIER_DB).get(0);
            username = citireProprietati(Constants.FISIER_DB).get(1);
            pass = citireProprietati(Constants.FISIER_DB).get(2);
        } catch (RuntimeException | ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DatabaseConnection() throws SQLException, IOException {
        if (databaseConnection == null) {
            databaseConnection = DriverManager.getConnection(dbUrl, username, pass);
        }
    }

    public static Connection getDatabaseConnection() throws SQLException {
        if (databaseConnection == null) {
            databaseConnection = DriverManager.getConnection(dbUrl, username, pass);
        }
        return databaseConnection;
    }
}

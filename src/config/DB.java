package pos.config;

import java.sql.*;

public class DB {
    private static final String DB_URL = "jdbc:sqlite:pos.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void init() {
        try (Connection conn = connect()) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("Connected to database: " + meta.getURL());
                System.out.println("Database Driver: " + meta.getDriverName());
                System.out.println("Initialization complete.");
            }
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database.");
            e.printStackTrace();
        }
    }
}

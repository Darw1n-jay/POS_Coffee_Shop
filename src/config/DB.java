package pos.config;

import java.sql.*;
import pos.dao.*;
import pos.model.*;

public class DB {
    private static final String DB_URL = "jdbc:sqlite:pos.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void init() {
        try (Connection conn = connect(); Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT, role TEXT)");
            st.execute("CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, price REAL, stock INTEGER)");
            st.execute("CREATE TABLE IF NOT EXISTS sales (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, datetime TEXT, total REAL, status TEXT)");
            st.execute("CREATE TABLE IF NOT EXISTS sale_items (id INTEGER PRIMARY KEY AUTOINCREMENT, sale_id INTEGER, product_id INTEGER, qty INTEGER, price REAL)");

            if (UserDAO.countAdmins(conn) == 0) {
                UserDAO.insert(new User("admin", "admin", "ADMIN"));
                System.out.println("Default admin created (admin/admin)");
            }
            if (ProductDAO.count(conn) == 0) {
                ProductDAO.insertSample(conn);
                System.out.println("Sample products added.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

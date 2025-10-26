package pos.dao;

import pos.config.DB;
import pos.model.User;
import pos.util.PasswordUtil;
import java.sql.*;
import java.util.*;

public class UserDAO {
    public static User getByUsername(String username) {
        try (Connection conn = DB.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username=?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.id = rs.getInt("id");
                u.username = rs.getString("username");
                u.password = rs.getString("password");
                u.role = rs.getString("role");
                return u;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static void insert(User u) {
        try (Connection conn = DB.connect();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO users (username,password,role) VALUES (?,?,?)")) {
            ps.setString(1, u.username);
            ps.setString(2, PasswordUtil.hash(u.password));
            ps.setString(3, u.role);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static List<User> getAll() {
        List<User> list = new ArrayList<>();
        try (Connection conn = DB.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM users")) {
            while (rs.next()) {
                User u = new User();
                u.id = rs.getInt("id");
                u.username = rs.getString("username");
                u.password = rs.getString("password");
                u.role = rs.getString("role");
                list.add(u);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static boolean verify(String username, String password) {
        User user = getByUsername(username);
        if (user == null) return false;
        return user.password.equals(PasswordUtil.hash(password));
    }
}

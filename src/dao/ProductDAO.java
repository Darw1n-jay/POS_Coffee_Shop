package pos.dao;

import pos.config.DB;
import pos.model.Product;
import java.sql.*;
import java.util.*;

public class ProductDAO {
    public static void insert(Product p) {
        try (Connection conn = DB.connect();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO products (name,price,stock) VALUES (?,?,?)")) {
            ps.setString(1, p.name);
            ps.setDouble(2, p.price);
            ps.setInt(3, p.stock);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static List<Product> getAll() {
        List<Product> list = new ArrayList<>();
        try (Connection conn = DB.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM products")) {
            while (rs.next()) list.add(new Product(rs.getInt("id"), rs.getString("name"), rs.getDouble("price"), rs.getInt("stock")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static Product getById(int id) {
        try (Connection conn = DB.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM products WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new Product(rs.getInt("id"), rs.getString("name"), rs.getDouble("price"), rs.getInt("stock"));
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static void update(Product p) {
        try (Connection conn = DB.connect();
             PreparedStatement ps = conn.prepareStatement("UPDATE products SET name=?,price=?,stock=? WHERE id=?")) {
            ps.setString(1, p.name);
            ps.setDouble(2, p.price);
            ps.setInt(3, p.stock);
            ps.setInt(4, p.id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void delete(int id) {
        try (Connection conn = DB.connect();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM products WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
    
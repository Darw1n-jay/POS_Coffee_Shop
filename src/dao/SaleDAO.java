package pos.dao;

import pos.config.DB;
import pos.model.*;
import java.sql.*;
import java.util.*;

public class SaleDAO {

    // Insert a new sale with sale items (transactional)
    public static void insert(Sale s, List<SaleItem> items) {
        try (Connection conn = DB.connect()) {
            conn.setAutoCommit(false);
            int saleId = 0;

            // Insert Sale
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO sales (user_id, datetime, total, status) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, s.userId);
                ps.setString(2, s.datetime);
                ps.setDouble(3, s.total);
                ps.setString(4, s.status);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) saleId = rs.getInt(1);
            }

            // Insert Sale Items + Update product stock
            try (PreparedStatement psi = conn.prepareStatement(
                        "INSERT INTO sale_items (sale_id, product_id, qty, price) VALUES (?, ?, ?, ?)");
                 PreparedStatement pst = conn.prepareStatement(
                        "UPDATE products SET stock = stock - ? WHERE id = ?")) {

                for (SaleItem si : items) {
                    // Insert each sale item
                    psi.setInt(1, saleId);
                    psi.setInt(2, si.productId);
                    psi.setInt(3, si.qty);
                    psi.setDouble(4, si.price);
                    psi.executeUpdate();

                    // Update product stock
                    pst.setInt(1, si.qty);
                    pst.setInt(2, si.productId);
                    pst.executeUpdate();
                }
            }

            conn.commit();
            System.out.println("✅ Sale recorded successfully (ID: " + saleId + ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get all sales
    public static List<Sale> getAll() {
        List<Sale> list = new ArrayList<>();
        try (Connection conn = DB.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM sales ORDER BY datetime DESC")) {
            while (rs.next()) {
                list.add(new Sale(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("datetime"),
                        rs.getDouble("total"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Get all sales for a specific user (e.g., cashier)
    public static List<Sale> getByUserId(int userId) {
        List<Sale> list = new ArrayList<>();
        try (Connection conn = DB.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM sales WHERE user_id = ? ORDER BY datetime DESC")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Sale(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("datetime"),
                        rs.getDouble("total"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Get sale items for a given sale ID
    public static List<SaleItem> getItemsBySaleId(int saleId) {
        List<SaleItem> items = new ArrayList<>();
        try (Connection conn = DB.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM sale_items WHERE sale_id = ?")) {
            ps.setInt(1, saleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.add(new SaleItem(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        rs.getInt("qty"),
                        rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // Delete a sale and its items (admin only)
    public static void delete(int id) {
        try (Connection conn = DB.connect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psi = conn.prepareStatement("DELETE FROM sale_items WHERE sale_id = ?");
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM sales WHERE id = ?")) {
                psi.setInt(1, id);
                psi.executeUpdate();

                ps.setInt(1, id);
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("🗑️ Sale " + id + " deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Count total number of sales
    public static int count() {
        try (Connection conn = DB.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM sales")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}

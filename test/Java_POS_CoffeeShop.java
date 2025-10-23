
import java.sql.*;
import java.util.*;
import java.time.*;

public class Java_POS_CoffeeShop {
    // --- Configuration ---
    private static final String DB_URL = "jdbc:sqlite:pos.db";

    // Current session
    private static User currentUser = null;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            DB.init();
            showWelcome();
            mainLoop();
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void showWelcome() {
        System.out.println("====================================");
        System.out.println("  Java POS - Coffee Shop (SQLite)");
        System.out.println("====================================");
    }

    private static void mainLoop() {
        while (true) {
            if (currentUser == null) {
                System.out.println("\n1) Login\n2) Register (cashier)\n3) Exit");
                System.out.print("Choose: ");
                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1": login(); break;
                    case "2": registerCashier(); break;
                    case "3": System.out.println("Bye"); System.exit(0); break;
                    default: System.out.println("Invalid choice");
                }
            } else {
                // show role-based menu
                if (currentUser.isAdmin()) adminMenu();
                else cashierMenu();
            }
        }
    }

    // --- Authentication ---
    private static void login() {
        System.out.print("Username: ");
        String u = scanner.nextLine().trim();
        System.out.print("Password: ");
        String p = scanner.nextLine().trim();
        User user = UserDAO.getByUsername(u);
        if (user != null && user.password.equals(p)) {
            currentUser = user;
            System.out.println("Logged in as " + currentUser.username + " (" + currentUser.role + ")");
        } else {
            System.out.println("Invalid credentials");
        }
    }

    private static void logout() {
        System.out.println("User " + currentUser.username + " logged out.");
        currentUser = null;
    }

    private static void registerCashier() {
        System.out.println("--- Register Cashier ---");
        System.out.print("Choose username: ");
        String u = scanner.nextLine().trim();
        if (UserDAO.getByUsername(u) != null) { System.out.println("Username exists"); return; }
        System.out.print("Choose password: ");
        String p = scanner.nextLine().trim();
        User user = new User(u, p, "CASHIER");
        UserDAO.insert(user);
        System.out.println("Cashier registered. You may login now.");
    }

    // --- Admin Menu ---
    private static void adminMenu() {
        System.out.println("\n--- Admin Menu ---");
        System.out.println("1) Products (add/update/delete/list)");
        System.out.println("2) Inventory (list)");
        System.out.println("3) Sales (create/view/void)");
        System.out.println("4) Manage Users (create admin/cashier, list)");
        System.out.println("5) Logout");
        System.out.print("Choose: ");
        String c = scanner.nextLine().trim();
        switch (c) {
            case "1": productManagement(); break;
            case "2": listProducts(); break;
            case "3": salesManagement(); break;
            case "4": manageUsers(); break;
            case "5": logout(); break;
            default: System.out.println("Invalid");
        }
    }

    // --- Cashier Menu ---
    private static void cashierMenu() {
        System.out.println("\n--- Cashier Menu ---");
        System.out.println("1) Create Sale");
        System.out.println("2) Update Sale");
        System.out.println("3) Delete Sale");
        System.out.println("4) List Products");
        System.out.println("5) List My Sales");
        System.out.println("6) Logout");
        System.out.print("Choose: ");
        String c = scanner.nextLine().trim();
        switch (c) {
            case "1": createSale(); break;
            case "2": updateSale(); break;
            case "3": deleteSale(); break;
            case "4": listProducts(); break;
            case "5": listSalesForCurrentUser(); break;
            case "6": logout(); break;
            default: System.out.println("Invalid");
        }
    }

    // --- Product management ---
    private static void productManagement() {
        System.out.println("\n--- Product Management ---");
        System.out.println("1) Add Product\n2) Update Product\n3) Delete Product\n4) List Products\n5) Back");
        System.out.print("Choose: ");
        String c = scanner.nextLine().trim();
        switch (c) {
            case "1": addProduct(); break;
            case "2": updateProduct(); break;
            case "3": deleteProduct(); break;
            case "4": listProducts(); break;
            case "5": return;
            default: System.out.println("Invalid");
        }
    }

    private static void addProduct() {
        System.out.print("Product name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Price: ");
        double price = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Stock qty: ");
        int qty = Integer.parseInt(scanner.nextLine().trim());
        Product p = new Product(0, name, price, qty);
        ProductDAO.insert(p);
        System.out.println("Product added");
    }

    private static void updateProduct() {
        listProducts();
        System.out.print("Enter product id to update: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        Product p = ProductDAO.getById(id);
        if (p == null) { System.out.println("Not found"); return; }
        System.out.print("New name (blank to keep) ["+p.name+"]: ");
        String name = scanner.nextLine().trim(); if (!name.isEmpty()) p.name = name;
        System.out.print("New price (blank to keep) ["+p.price+"]: ");
        String sp = scanner.nextLine().trim(); if (!sp.isEmpty()) p.price = Double.parseDouble(sp);
        System.out.print("New stock qty (blank to keep) ["+p.stock+"]: ");
        String sq = scanner.nextLine().trim(); if (!sq.isEmpty()) p.stock = Integer.parseInt(sq);
        ProductDAO.update(p);
        System.out.println("Updated");
    }

    private static void deleteProduct() {
        listProducts();
        System.out.print("Enter product id to delete: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        ProductDAO.delete(id);
        System.out.println("Deleted (if existed)");
    }

    private static void listProducts() {
        System.out.println("\n--- Products / Inventory ---");
        List<Product> list = ProductDAO.getAll();
        System.out.printf("%5s %20s %10s %8s\n", "ID","NAME","PRICE","STOCK");
        for (Product p : list) {
            System.out.printf("%5d %20s %10.2f %8d\n", p.id, p.name, p.price, p.stock);
        }
    }

    // --- Sales ---
    private static void salesManagement() {
        System.out.println("\n--- Sales Management ---");
        System.out.println("1) Create Sale\n2) View All Sales\n3) Void (mark) Sale\n4) Back");
        System.out.print("Choose: ");
        String c = scanner.nextLine().trim();
        switch (c) {
            case "1": createSale(); break;
            case "2": listAllSales(); break;
            case "3": voidSale(); break;
            case "4": return;
            default: System.out.println("Invalid");
        }
    }

    private static void createSale() {
        System.out.println("\n--- Create Sale ---");
        List<SaleItem> items = new ArrayList<>();
        while (true) {
            listProducts();
            System.out.print("Enter Product ID (or 0 to finish): ");
            int pid = Integer.parseInt(scanner.nextLine().trim());
            if (pid == 0) break;
            Product p = ProductDAO.getById(pid);
            if (p == null) { System.out.println("Not found"); continue; }
            System.out.print("Qty: ");
            int q = Integer.parseInt(scanner.nextLine().trim());
            if (q <= 0) { System.out.println("Invalid qty"); continue; }
            if (q > p.stock) { System.out.println("Not enough stock ("+p.stock+")"); continue; }
            items.add(new SaleItem(0, pid, q, p.price));
            System.out.println("Added " + p.name + " x" + q);
        }
        if (items.isEmpty()) { System.out.println("No items. Cancelled."); return; }
        // Calculate total
        double total = 0;
        for (SaleItem si : items) total += si.price * si.qty;
        System.out.printf("Total: %.2f\n", total);
        System.out.print("Confirm sale? (y/n): ");
        String c = scanner.nextLine().trim().toLowerCase();
        if (!c.equals("y")) { System.out.println("Cancelled"); return; }
        // Insert sale
        Sale s = new Sale(0, currentUser.id, LocalDateTime.now().toString(), total, "COMPLETED");
        int saleId = SaleDAO.insert(s, items);
        System.out.println("Sale created: ID=" + saleId);
    }

    private static void listAllSales() {
        System.out.println("\n--- All Sales ---");
        List<Sale> list = SaleDAO.getAll();
        System.out.printf("%5s %10s %20s %10s %10s\n", "ID","USER_ID","DATETIME","TOTAL","STATUS");
        for (Sale s : list) System.out.printf("%5d %10d %20s %10.2f %10s\n", s.id, s.userId, s.datetime, s.total, s.status);
        System.out.print("Enter sale id to see items (or blank): ");
        String in = scanner.nextLine().trim();
        if (!in.isEmpty()) {
            int id = Integer.parseInt(in);
            List<SaleItem> items = SaleDAO.getItems(id);
            System.out.println("Items:");
            System.out.printf("%5s %10s %8s %10s\n", "ID","PRODUCT","QTY","PRICE");
            for (SaleItem si : items) {
                Product p = ProductDAO.getById(si.productId);
                System.out.printf("%5d %10s %8d %10.2f\n", si.id, p!=null? p.name: "-", si.qty, si.price);
            }
        }
    }

    private static void listSalesForCurrentUser() {
        System.out.println("\n--- My Sales ---");
        List<Sale> list = SaleDAO.getByUserId(currentUser.id);
        System.out.printf("%5s %20s %10s %10s\n", "ID","DATETIME","TOTAL","STATUS");
        for (Sale s : list) System.out.printf("%5d %20s %10.2f %10s\n", s.id, s.datetime, s.total, s.status);
    }

    private static void voidSale() {
        listAllSales();
        System.out.print("Enter sale id to VOID/cancel: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        Sale s = SaleDAO.getById(id);
        if (s == null) { System.out.println("Not found"); return; }
        System.out.print("Confirm void sale id " + id + "? (y/n): ");
        String c = scanner.nextLine().trim().toLowerCase();
        if (!c.equals("y")) { System.out.println("Cancelled"); return; }
        SaleDAO.voidSale(id);
        System.out.println("Sale voided (status set to VOID). Stock restored where applicable.");
    }

    // Cashier update & delete
    private static void updateSale() {
        listSalesForCurrentUser();
        System.out.print("Enter sale id to update: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        Sale s = SaleDAO.getById(id);
        if (s == null) { System.out.println("Not found"); return; }
        if (s.userId != currentUser.id && !currentUser.isAdmin()) { System.out.println("Not allowed"); return; }
        if (s.status.equals("VOID")) { System.out.println("Cannot update voided sale"); return; }
        // Simple update: allow changing status to CANCELLED
        System.out.println("1) Cancel (mark CANCELLED)\n2) Back");
        System.out.print("Choose: ");
        String c = scanner.nextLine().trim();
        if (c.equals("1")) {
            SaleDAO.cancelSale(id);
            System.out.println("Sale cancelled and stock restored where applicable.");
        }
    }

    private static void deleteSale() {
        listSalesForCurrentUser();
        System.out.print("Enter sale id to DELETE: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        Sale s = SaleDAO.getById(id);
        if (s == null) { System.out.println("Not found"); return; }
        if (s.userId != currentUser.id && !currentUser.isAdmin()) { System.out.println("Not allowed"); return; }
        System.out.print("Confirm delete? (y/n): ");
        String c = scanner.nextLine().trim().toLowerCase();
        if (!c.equals("y")) { System.out.println("Aborted"); return; }
        SaleDAO.delete(id);
        System.out.println("Deleted");
    }

    // --- User management (admin) ---
    private static void manageUsers() {
        System.out.println("\n--- Manage Users ---");
        System.out.println("1) Create User\n2) List Users\n3) Back");
        System.out.print("Choose: ");
        String c = scanner.nextLine().trim();
        switch (c) {
            case "1": createUser(); break;
            case "2": listUsers(); break;
            case "3": return;
            default: System.out.println("Invalid");
        }
    }

    private static void createUser() {
        System.out.print("Username: "); String u = scanner.nextLine().trim();
        if (UserDAO.getByUsername(u) != null) { System.out.println("Exists"); return; }
        System.out.print("Password: "); String p = scanner.nextLine().trim();
        System.out.print("Role (ADMIN/CASHIER): "); String r = scanner.nextLine().trim().toUpperCase();
        if (!r.equals("ADMIN") && !r.equals("CASHIER")) { System.out.println("Invalid role"); return; }
        UserDAO.insert(new User(u, p, r));
        System.out.println("Created");
    }

    private static void listUsers() {
        System.out.println("\n--- Users ---");
        List<User> list = UserDAO.getAll();
        System.out.printf("%5s %15s %10s\n", "ID","USERNAME","ROLE");
        for (User u : list) System.out.printf("%5d %15s %10s\n", u.id, u.username, u.role);
    }

    // --- Database & DAOs ---
    static class DB {
        static void init() {
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                try (Statement st = conn.createStatement()) {
                    // users
                    st.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT, role TEXT)");
                    // products
                    st.execute("CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, price REAL, stock INTEGER)");
                    // sales
                    st.execute("CREATE TABLE IF NOT EXISTS sales (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, datetime TEXT, total REAL, status TEXT)");
                    // sale items
                    st.execute("CREATE TABLE IF NOT EXISTS sale_items (id INTEGER PRIMARY KEY AUTOINCREMENT, sale_id INTEGER, product_id INTEGER, qty INTEGER, price REAL)");
                }
                // create default admin if none
                if (UserDAO.countAdmins(conn) == 0) {
                    try (PreparedStatement ps = conn.prepareStatement("INSERT INTO users (username,password,role) VALUES (?,?,?)")) {
                        ps.setString(1, "admin"); ps.setString(2, "admin"); ps.setString(3, "ADMIN"); ps.executeUpdate();
                        System.out.println("Default admin created -> username: admin password: admin");
                    }
                }
                // sample products if none
                if (ProductDAO.count(conn) == 0) {
                    ProductDAO.insertSample(conn);
                    System.out.println("Sample products inserted.");
                }
            } catch (SQLException e) { throw new RuntimeException(e); }
        }
    }

    // --- Models & DAO implementations ---
    static class User { public int id; public String username; public String password; public String role; public User() {}
        public User(String u, String p, String r) { this.username=u; this.password=p; this.role=r; }
        public boolean isAdmin() { return "ADMIN".equalsIgnoreCase(role); }
    }

    static class UserDAO {
        static User getByUsername(String username) {
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                try (PreparedStatement ps = conn.prepareStatement("SELECT id,username,password,role FROM users WHERE username=?")) {
                    ps.setString(1, username);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) { User u = new User(); u.id=rs.getInt(1); u.username=rs.getString(2); u.password=rs.getString(3); u.role=rs.getString(4); return u; }
                    }
                }
            } catch (SQLException e) { e.printStackTrace(); }
            return null;
        }
        static int countAdmins(Connection conn) throws SQLException {
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users WHERE role='ADMIN'")) { return rs.next()? rs.getInt(1) : 0; }
        }
        static void insert(User u) {
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO users (username,password,role) VALUES (?,?,?)")) {
                    ps.setString(1, u.username); ps.setString(2, u.password); ps.setString(3, u.role); ps.executeUpdate();
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }
        static List<User> getAll() {
            List<User> out = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection(DB_URL); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT id,username,password,role FROM users")) {
                while (rs.next()) { User u = new User(); u.id=rs.getInt(1); u.username=rs.getString(2); u.password=rs.getString(3); u.role=rs.getString(4); out.add(u); }
            } catch (SQLException e) { e.printStackTrace(); }
            return out;
        }
    }

    static class Product { public int id; public String name; public double price; public int stock; public Product() {}
        public Product(int id, String name, double price, int stock) { this.id=id; this.name=name; this.price=price; this.stock=stock; }
    }

    static class ProductDAO {
        static void insert(Product p) {
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO products (name,price,stock) VALUES (?,?,?)")) {
                    ps.setString(1, p.name); ps.setDouble(2, p.price); ps.setInt(3, p.stock); ps.executeUpdate();
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }
        static void insertSample(Connection conn) throws SQLException {
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO products (name,price,stock) VALUES (?,?,?)")) {
                String[] names = {"Espresso","Americano","Latte","Cappuccino","Mocha"};
                double[] prices = {80,90,120,120,130};
                int[] stocks = {50,50,50,50,50};
                for (int i=0;i<names.length;i++) { ps.setString(1,names[i]); ps.setDouble(2,prices[i]); ps.setInt(3,stocks[i]); ps.executeUpdate(); }
            }
        }
        static Product getById(int id) {
            try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement ps = conn.prepareStatement("SELECT id,name,price,stock FROM products WHERE id=?")) {
                ps.setInt(1,id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return new Product(rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getInt(4));
                }
            } catch (SQLException e) { e.printStackTrace(); }
            return null;
        }
        static List<Product> getAll() {
            List<Product> out = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection(DB_URL); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT id,name,price,stock FROM products")) {
                while (rs.next()) out.add(new Product(rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getInt(4)));
            } catch (SQLException e) { e.printStackTrace(); }
            return out;
        }
        static void update(Product p) {
            try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement ps = conn.prepareStatement("UPDATE products SET name=?,price=?,stock=? WHERE id=?")) {
                ps.setString(1,p.name); ps.setDouble(2,p.price); ps.setInt(3,p.stock); ps.setInt(4,p.id); ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }
        static void delete(int id) {
            try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement ps = conn.prepareStatement("DELETE FROM products WHERE id=?")) {
                ps.setInt(1,id); ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }
        static int count(Connection conn) throws SQLException { try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM products")) { return rs.next()? rs.getInt(1): 0; } }
    }

    static class Sale { public int id; public int userId; public String datetime; public double total; public String status; public Sale() {}
        public Sale(int id, int userId, String datetime, double total, String status) { this.id=id; this.userId=userId; this.datetime=datetime; this.total=total; this.status=status; }
    }

    static class SaleItem { public int id; public int productId; public int qty; public double price; public SaleItem() {}
        public SaleItem(int id, int productId, int qty, double price) { this.id=id; this.productId=productId; this.qty=qty; this.price=price; }
    }

    static class SaleDAO {
        static int insert(Sale s, List<SaleItem> items) {
            int saleId = -1;
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                conn.setAutoCommit(false);
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO sales (user_id,datetime,total,status) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, s.userId); ps.setString(2, s.datetime); ps.setDouble(3, s.total); ps.setString(4, s.status); ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) saleId = rs.getInt(1); }
                }
                // insert items and deduct stock
                try (PreparedStatement psi = conn.prepareStatement("INSERT INTO sale_items (sale_id,product_id,qty,price) VALUES (?,?,?,?)");
                     PreparedStatement pst = conn.prepareStatement("UPDATE products SET stock = stock - ? WHERE id = ?")) {
                    for (SaleItem si : items) {
                        psi.setInt(1, saleId); psi.setInt(2, si.productId); psi.setInt(3, si.qty); psi.setDouble(4, si.price); psi.executeUpdate();
                        pst.setInt(1, si.qty); pst.setInt(2, si.productId); pst.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) { e.printStackTrace(); }
            return saleId;
        }
        static List<Sale> getAll() {
            List<Sale> out = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection(DB_URL); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT id,user_id,datetime,total,status FROM sales ORDER BY id DESC")) {
                while (rs.next()) out.add(new Sale(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getDouble(4), rs.getString(5)));
            } catch (SQLException e) { e.printStackTrace(); }
            return out;
        }
        static List<Sale> getByUserId(int userId) {
            List<Sale> out = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement ps = conn.prepareStatement("SELECT id,user_id,datetime,total,status FROM sales WHERE user_id=? ORDER BY id DESC")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) { while (rs.next()) out.add(new Sale(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getDouble(4), rs.getString(5))); }
            } catch (SQLException e) { e.printStackTrace(); }
            return out;
        }
        static Sale getById(int id) {
            try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement ps = conn.prepareStatement("SELECT id,user_id,datetime,total,status FROM sales WHERE id=?")) {
                ps.setInt(1,id);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return new Sale(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getDouble(4), rs.getString(5)); }
            } catch (SQLException e) { e.printStackTrace(); }
            return null;
        }
        static List<SaleItem> getItems(int saleId) {
            List<SaleItem> out = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement ps = conn.prepareStatement("SELECT id,product_id,qty,price FROM sale_items WHERE sale_id=?")) {
                ps.setInt(1, saleId);
                try (ResultSet rs = ps.executeQuery()) { while (rs.next()) out.add(new SaleItem(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getDouble(4))); }
            } catch (SQLException e) { e.printStackTrace(); }
            return out;
        }
        static void voidSale(int saleId) {
            // set status VOID and restore stock
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                conn.setAutoCommit(false);
                List<SaleItem> items = getItems(saleId);
                try (PreparedStatement psu = conn.prepareStatement("UPDATE products SET stock = stock + ? WHERE id = ?")) {
                    for (SaleItem si : items) { psu.setInt(1, si.qty); psu.setInt(2, si.productId); psu.executeUpdate(); }
                }
                try (PreparedStatement pss = conn.prepareStatement("UPDATE sales SET status='VOID' WHERE id=?")) { pss.setInt(1,saleId); pss.executeUpdate(); }
                conn.commit();
            } catch (SQLException e) { e.printStackTrace(); }
        }
        static void cancelSale(int saleId) { voidSale(saleId); // same behavior for now
        }
        static void delete(int saleId) {
            // delete items then sale (no stock restore) - use with caution
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM sale_items WHERE sale_id=?")) { ps.setInt(1,saleId); ps.executeUpdate(); }
                try (PreparedStatement ps2 = conn.prepareStatement("DELETE FROM sales WHERE id=?")) { ps2.setInt(1,saleId); ps2.executeUpdate(); }
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}

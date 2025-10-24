package pos;

import pos.config.DB;
import pos.model.*;
import pos.dao.*;

import java.util.*;
import java.time.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static User currentUser = null;

    public static void main(String[] args) {
        DB.init();
        showWelcome();
        mainLoop();
    }

    private static void showWelcome() {
        System.out.println("====================================");
        System.out.println("       Java POS - Coffee Shop ");
        System.out.println("====================================");
    }

    private static void mainLoop() {
        while (true) {
            if (currentUser == null) {
                System.out.println("\n1) Login\n2) Register (Cashier)\n3) Exit");
                System.out.print("Choose: ");
                switch (scanner.nextLine().trim()) {
                    case "1": login(); break;
                    case "2": registerCashier(); break;
                    case "3": System.exit(0);
                    default: System.out.println("Invalid choice");
                }
            } else {
                if (currentUser.isAdmin()) adminMenu();
                else cashierMenu();
            }
        }
    }

    
    private static void login() {
        System.out.print("Username: ");
        String u = scanner.nextLine().trim();
        System.out.print("Password: ");
        String p = scanner.nextLine().trim();
        User user = UserDAO.getByUsername(u);
        if (user != null && user.password.equals(p)) {
            currentUser = user;
            System.out.println("Logged in as " + user.username + " (" + user.role + ")");
        } else System.out.println("Invalid credentials");
    }

    private static void registerCashier() {
        System.out.print("Username: ");
        String u = scanner.nextLine().trim();
        if (UserDAO.getByUsername(u) != null) { System.out.println("Username exists"); return; }
        System.out.print("Password: ");
        String p = scanner.nextLine().trim();
        UserDAO.insert(new User(u, p, "CASHIER"));
        System.out.println("Cashier registered!");
    }

    private static void logout() {
        currentUser = null;
        System.out.println("Logged out.");
    }

    
    private static void adminMenu() {
        System.out.println("\n--- Admin Menu ---");
        System.out.println("1) Manage Products\n2) View Inventory\n3) Manage Sales\n4) Manage Users\n5) Logout");
        System.out.print("Choose: ");
        switch (scanner.nextLine().trim()) {
            case "1": productManagement(); break;
            case "2": listProducts(); break;
            case "3": salesManagement(); break;
            case "4": manageUsers(); break;
            case "5": logout(); break;
            default: System.out.println("Invalid");
        }
    }

    private static void cashierMenu() {
        System.out.println("\n--- Cashier Menu ---");
        System.out.println("1) Create Sale\n2) List My Sales\n3) List Products\n4) Logout");
        System.out.print("Choose: ");
        switch (scanner.nextLine().trim()) {
            case "1": createSale(); break;
            case "2": listSalesForCurrentUser(); break;
            case "3": listProducts(); break;
            case "4": logout(); break;
            default: System.out.println("Invalid");
        }
    }

    
    private static void productManagement() {
        System.out.println("\n1) Add Product\n2) Update Product\n3) Delete Product\n4) List\n5) Back");
        System.out.print("Choose: ");
        switch (scanner.nextLine().trim()) {
            case "1": addProduct(); break;
            case "2": updateProduct(); break;
            case "3": deleteProduct(); break;
            case "4": listProducts(); break;
            case "5": return;
        }
    }

    private static void addProduct() {
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Price: ");
        double price = Double.parseDouble(scanner.nextLine());
        System.out.print("Stock: ");
        int stock = Integer.parseInt(scanner.nextLine());
        ProductDAO.insert(new Product(0, name, price, stock));
        System.out.println("Added.");
    }

    private static void updateProduct() {
        listProducts();
        System.out.print("ID to update: ");
        int id = Integer.parseInt(scanner.nextLine());
        Product p = ProductDAO.getById(id);
        if (p == null) { System.out.println("Not found."); return; }
        System.out.print("New name ("+p.name+"): ");
        String n = scanner.nextLine();
        if (!n.isEmpty()) p.name = n;
        ProductDAO.update(p);
        System.out.println("Updated.");
    }

    private static void deleteProduct() {
        listProducts();
        System.out.print("ID to delete: ");
        ProductDAO.delete(Integer.parseInt(scanner.nextLine()));
        System.out.println("Deleted.");
    }

    private static void listProducts() {
        System.out.println("\n--- Products ---");
        for (Product p : ProductDAO.getAll())
            System.out.printf("%3d %-15s %8.2f %5d\n", p.id, p.name, p.price, p.stock);
    }

    
    private static void createSale() {
        List<SaleItem> items = new ArrayList<>();
        while (true) {
            listProducts();
            System.out.print("Enter Product ID (0 to finish): ");
            int id = Integer.parseInt(scanner.nextLine());
            if (id == 0) break;
            Product p = ProductDAO.getById(id);
            if (p == null) continue;
            System.out.print("Qty: ");
            int qty = Integer.parseInt(scanner.nextLine());
            items.add(new SaleItem(0, id, qty, p.price));
        }
        if (items.isEmpty()) return;
        double total = items.stream().mapToDouble(i -> i.qty * i.price).sum();
        Sale s = new Sale(0, currentUser.id, LocalDateTime.now().toString(), total, "COMPLETED");
        SaleDAO.insert(s, items);
        System.out.println("Sale completed. Total: " + total);
    }

    private static void listSalesForCurrentUser() {
        System.out.println("\n--- My Sales ---");
        for (Sale s : SaleDAO.getByUserId(currentUser.id))
            System.out.printf("%3d %s %.2f %s\n", s.id, s.datetime, s.total, s.status);
    }

    private static void salesManagement() {
        for (Sale s : SaleDAO.getAll())
            System.out.printf("%3d User:%d %.2f %s\n", s.id, s.userId, s.total, s.status);
    }

    
    private static void manageUsers() {
        System.out.println("\n1) Add User\n2) List Users\n3) Back");
        switch (scanner.nextLine().trim()) {
            case "1": createUser(); break;
            case "2": listUsers(); break;
        }
    }

    private static void createUser() {
        System.out.print("Username: ");
        String u = scanner.nextLine();
        System.out.print("Password: ");
        String p = scanner.nextLine();
        System.out.print("Role (ADMIN/CASHIER): ");
        String r = scanner.nextLine().toUpperCase();
        UserDAO.insert(new User(u, p, r));
        System.out.println("User created.");
    }

    private static void listUsers() {
        for (User u : UserDAO.getAll())
            System.out.printf("%3d %-10s %s\n", u.id, u.username, u.role);
    }
}

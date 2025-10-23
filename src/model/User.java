package pos.model;

public class User {
    public int id;
    public String username;
    public String password;
    public String role;

    public User() {}
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}

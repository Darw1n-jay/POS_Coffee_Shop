package pos.model;

public class Sale {
    public int id;
    public int userId;
    public String datetime;
    public double total;
    public String status;

    public Sale() {}
    public Sale(int id, int userId, String datetime, double total, String status) {
        this.id = id;
        this.userId = userId;
        this.datetime = datetime;
        this.total = total;
        this.status = status;
    }
}

package experimental.database;

public class TestConnectionObject {
    static void main() {
        ConnectionObject c = new ConnectionObject();
        c.getConnection();
        System.out.println("Opened database successfully");
    }
}

public class DBTest {
    public static void main(String[] args) {
        if (DBConnection.getConnection() != null) {
            System.out.println("MySQL Connected Successfully!");
        } else {
            System.out.println("Connection Failed!");
        }
    }
}

















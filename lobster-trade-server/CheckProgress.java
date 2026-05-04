import java.sql.*;

public class CheckProgress {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/lobster_trade?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
        Connection conn = DriverManager.getConnection(url, "lobster", "lobster123");
        Statement stmt = conn.createStatement();

        // Check if order_progress table exists
        ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'order_progress'");
        if (rs.next()) {
            System.out.println("order_progress table EXISTS");
            rs = stmt.executeQuery("DESCRIBE order_progress");
            while (rs.next()) System.out.println("  " + rs.getString("Field") + " " + rs.getString("Type"));
        } else {
            System.out.println("order_progress table DOES NOT EXIST");
        }

        conn.close();
    }
}
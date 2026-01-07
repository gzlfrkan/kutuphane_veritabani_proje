import java.sql.*;

public class Database {
    private static Connection conn;
    private static final String URL = "jdbc:postgresql://localhost:5432/kutuphane";
    private static final String USER = "postgres";
    private static final String PASSWORD = "cool";

    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return conn;
    }
}

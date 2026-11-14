import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC implementation. The connection is opened and closed inside try-with-resources
 * and the SQL uses placeholders, so user text can never change the statement.
 * Table: CREATE TABLE fees (id BIGINT AUTO_INCREMENT PRIMARY KEY,
 *                           reg_no VARCHAR(9) NOT NULL, amount DECIMAL(10,2) NOT NULL);
 */
public class JdbcFeeRepository implements FeeRepository {
    private final String url, user, password;

    public JdbcFeeRepository(String url, String user, String password) {
        this.url = url; this.user = user; this.password = password;
    }

    @Override
    public long save(String regNo, BigDecimal amount) throws SQLException {
        String sql = "INSERT INTO fees(reg_no, amount) VALUES (?, ?)";
        try (Connection c = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, regNo);
            ps.setBigDecimal(2, amount);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : -1;
            }
        }
    }
}

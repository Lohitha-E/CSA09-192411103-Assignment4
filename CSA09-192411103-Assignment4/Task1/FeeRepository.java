import java.math.BigDecimal;
import java.sql.SQLException;

/** Data-access contract used by FeeService. The window never sees this interface. */
public interface FeeRepository {
    /** Stores one payment and returns its generated id. */
    long save(String regNo, BigDecimal amount) throws SQLException;
}

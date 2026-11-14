import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Test double / default repository so the program runs without a database. */
public class InMemoryFeeRepository implements FeeRepository {
    private final List<String> rows = new ArrayList<>();

    @Override
    public synchronized long save(String regNo, BigDecimal amount) {
        rows.add(regNo + "," + amount);
        return rows.size();
    }

    public synchronized int count() { return rows.size(); }
}

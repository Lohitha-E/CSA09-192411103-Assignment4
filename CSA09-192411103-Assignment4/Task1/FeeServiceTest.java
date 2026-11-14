import java.math.BigDecimal;

/** Plain-Java test: no window needed, which is the point of separating FeeService. */
public class FeeServiceTest {
    static int failures = 0;

    static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (!ok) failures++;
    }

    static boolean rejects(FeeService s, String reg, String amt) {
        try { s.validate(reg, amt); return false; }
        catch (FeeService.ValidationException e) { return true; }
    }

    public static void main(String[] args) throws Exception {
        InMemoryFeeRepository repo = new InMemoryFeeRepository();
        FeeService s = new FeeService(repo, 0);

        check("valid input accepted", s.validate("192411103", "2500.50").compareTo(new BigDecimal("2500.50")) == 0);
        check("empty register number rejected", rejects(s, "", "100"));
        check("short register number rejected", rejects(s, "1924", "100"));
        check("letters in register number rejected", rejects(s, "19241110A", "100"));
        check("empty amount rejected", rejects(s, "192411103", ""));
        check("text amount rejected", rejects(s, "192411103", "abc"));
        check("negative amount rejected", rejects(s, "192411103", "-5"));
        check("zero amount rejected", rejects(s, "192411103", "0"));
        check("3 decimal places rejected", rejects(s, "192411103", "10.123"));
        check("amount above limit rejected", rejects(s, "192411103", "1000001"));
        check("SQL text rejected as amount", rejects(s, "192411103", "1); DROP TABLE fees;--"));

        FeeService.Receipt r = s.pay("192411103", "750");
        check("payment returns receipt", r.id == 1 && r.amount.compareTo(new BigDecimal("750")) == 0);
        check("exactly one row saved", repo.count() == 1);
        check("invalid payment saves nothing", rejects(s, "192411103", "x") && repo.count() == 1);

        FeeService failing = new FeeService((reg, amt) -> { throw new java.sql.SQLException("db down"); }, 0);
        try { failing.pay("192411103", "100"); check("database failure reported", false); }
        catch (FeeService.PaymentException e) { check("database failure wrapped, cause kept", e.getCause().getMessage().equals("db down")); }

        System.out.println(failures == 0 ? "All tests passed." : failures + " test(s) failed.");
    }
}

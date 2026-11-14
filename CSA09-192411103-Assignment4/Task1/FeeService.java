import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Application logic for fee payment: validation and payment. It has NO Swing or AWT
 * imports, so it can be tested without opening a window.
 */
public class FeeService {

    /** Thrown for input the user can correct (empty, malformed or out-of-range). */
    public static class ValidationException extends Exception {
        public ValidationException(String message) { super(message); }
    }

    /** Thrown when the payment could not be stored; the original SQLException is the cause. */
    public static class PaymentException extends Exception {
        public PaymentException(String message, Throwable cause) { super(message, cause); }
    }

    /** Immutable result shown by the window (and by the Receipt button). */
    public static final class Receipt {
        public final long id;
        public final String regNo;
        public final BigDecimal amount;
        Receipt(long id, String regNo, BigDecimal amount) {
            this.id = id; this.regNo = regNo; this.amount = amount;
        }
        @Override public String toString() {
            return "Receipt #" + id + "\nRegister number: " + regNo + "\nAmount paid: Rs. " + amount;
        }
    }

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000");

    private final FeeRepository repository;
    private final long bankConfirmationDelayMs;

    public FeeService(FeeRepository repository, long bankConfirmationDelayMs) {
        this.repository = repository;
        this.bankConfirmationDelayMs = bankConfirmationDelayMs;
    }

    /** Pure validation: no I/O, fast, safe to call on the event-dispatch thread. */
    public BigDecimal validate(String regNo, String amountText) throws ValidationException {
        if (regNo == null || !regNo.trim().matches("\\d{9}")) {
            throw new ValidationException("Register number must be exactly 9 digits.");
        }
        if (amountText == null || amountText.trim().isEmpty()) {
            throw new ValidationException("Enter the amount to pay.");
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountText.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Amount must be a number, for example 2500.50.");
        }
        if (amount.signum() <= 0) throw new ValidationException("Amount must be greater than zero.");
        if (amount.scale() > 2) throw new ValidationException("Amount can have at most 2 decimal places.");
        if (amount.compareTo(MAX_AMOUNT) > 0) throw new ValidationException("Amount cannot exceed Rs. 1,000,000.");
        return amount;
    }

    /** Blocking: validates, stores and waits for the (simulated) bank. Call from a worker thread. */
    public Receipt pay(String regNo, String amountText)
            throws ValidationException, PaymentException, InterruptedException {
        BigDecimal amount = validate(regNo, amountText);
        long id;
        try {
            id = repository.save(regNo.trim(), amount);
        } catch (SQLException e) {
            throw new PaymentException("The payment could not be saved. Please try again.", e);
        }
        Thread.sleep(bankConfirmationDelayMs);   // stands in for the bank confirmation
        return new Receipt(id, regNo.trim(), amount);
    }
}

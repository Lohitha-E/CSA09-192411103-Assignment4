import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * The window only collects input and shows results. Each button has its own listener;
 * all rules and all database work live in FeeService.
 */
public class FeeWindow extends JFrame {
    private final JTextField regNo = new JTextField(12);
    private final JTextField amount = new JTextField(12);
    private final JButton pay = new JButton("Pay");
    private final JButton clear = new JButton("Clear");
    private final JButton receipt = new JButton("Receipt");
    private final JLabel status = new JLabel("Enter details and press Pay.");

    private final FeeService service;
    private FeeService.Receipt lastReceipt;
    private boolean paymentInProgress;

    public FeeWindow(FeeService service) {
        super("Fee Payment");
        this.service = service;

        JPanel form = new JPanel(new GridLayout(2, 2, 5, 5));
        form.add(new JLabel("Register number:")); form.add(regNo);
        form.add(new JLabel("Amount (Rs.):"));    form.add(amount);

        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(pay); buttons.add(clear); buttons.add(receipt);
        receipt.setEnabled(false);                      // nothing to show before the first payment

        add(form, BorderLayout.NORTH);
        add(buttons, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);

        // one small listener per button: no if/else chain, no string comparison
        pay.addActionListener(e -> onPay());
        clear.addActionListener(e -> onClear());
        receipt.addActionListener(e -> onReceipt());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { onClose(); }
        });
        pack();
        setLocationRelativeTo(null);
    }

    private void onPay() {
        try {
            service.validate(regNo.getText(), amount.getText());      // fast, on the EDT
        } catch (FeeService.ValidationException ex) {
            status.setText(ex.getMessage());
            return;
        }
        paymentInProgress = true;
        pay.setEnabled(false);
        status.setText("Processing payment...");

        new SwingWorker<FeeService.Receipt, Void>() {                // slow work off the EDT
            @Override protected FeeService.Receipt doInBackground() throws Exception {
                return service.pay(regNo.getText(), amount.getText());
            }
            @Override protected void done() {
                paymentInProgress = false;
                pay.setEnabled(true);
                try {
                    lastReceipt = get();
                    receipt.setEnabled(true);
                    status.setText("Payment successful. Receipt #" + lastReceipt.id);
                } catch (ExecutionException ex) {
                    status.setText(ex.getCause().getMessage());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void onClear() {
        regNo.setText("");
        amount.setText("");
        status.setText("Cleared.");
        regNo.requestFocusInWindow();
    }

    private void onReceipt() {
        JOptionPane.showMessageDialog(this, lastReceipt.toString(), "Receipt", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onClose() {
        if (paymentInProgress) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "A payment is still being processed. Close anyway?", "Payment in progress",
                    JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) return;
        }
        dispose();
    }

    public static void main(String[] args) {
        // Default: in-memory repository. For MySQL run with
        //   -Dfees.url=jdbc:mysql://localhost:3306/college -Dfees.user=... -Dfees.password=...
        FeeRepository repo = System.getProperty("fees.url") == null
                ? new InMemoryFeeRepository()
                : new JdbcFeeRepository(System.getProperty("fees.url"),
                        System.getProperty("fees.user"), System.getProperty("fees.password"));
        FeeService service = new FeeService(repo, 3000);
        SwingUtilities.invokeLater(() -> new FeeWindow(service).setVisible(true));
    }
}

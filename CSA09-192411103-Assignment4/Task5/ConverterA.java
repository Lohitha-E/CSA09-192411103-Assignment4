import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/** Task 5 - style (a) frame implements ActionListener, original requirement. */
public class ConverterA extends JFrame implements ActionListener {
    private final JTextField amount = new JTextField(10);
    private final JComboBox<String> target = new JComboBox<>(new String[]{"USD", "EUR"});
    private final JLabel result = new JLabel(" ");
    private final JButton convertButton = new JButton("Convert");

    private static double rate(String currency) { return currency.equals("USD") ? 0.012 : 0.011; }

    public ConverterA() {
        super("Currency Converter - (a) frame implements ActionListener");
        setLayout(new FlowLayout());
        add(new JLabel("INR")); add(amount); add(target); add(convertButton); add(result);
        convertButton.addActionListener(this);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { dispose(); }
        });
        pack();
        setSize(460, 90);
    }

    private String converted() {
        try {
            double inr = Double.parseDouble(amount.getText().trim());
            if (!Double.isFinite(inr) || inr < 0) return "Invalid amount";
            return String.format("%.2f %s", inr * rate((String) target.getSelectedItem()), target.getSelectedItem());
        } catch (NumberFormatException ex) { return "Invalid amount"; }
    }

    private void showResult() { result.setText(converted()); }

    @Override public void actionPerformed(ActionEvent e) { showResult(); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConverterA().setVisible(true));
    }
}

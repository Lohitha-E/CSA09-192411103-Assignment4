package events.ui;

import events.dao.RegistrationDao.DaoException;
import events.dao.RegistrationDao.DuplicateRegistrationException;
import events.model.Event;
import events.model.Registration;
import events.service.RegistrationService;
import events.service.RegistrationService.ValidationException;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/** Registration form. Validation runs on the EDT; the database work runs in a SwingWorker. */
class RegistrationFormPanel extends JPanel {
    private final RegistrationService service;
    private final Consumer<String> status;
    private final JTextField studentId = new JTextField(12);
    private final JTextField name = new JTextField(20);
    private final JTextField email = new JTextField(20);
    private final JComboBox<Event> event = new JComboBox<>();
    private final JButton register = new JButton("Register");
    private final JButton clear = new JButton("Clear");

    RegistrationFormPanel(RegistrationService service, Consumer<String> status) {
        this.service = service;
        this.status = status;
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        addRow(c, 0, "Student ID:", studentId);
        addRow(c, 1, "Name:", name);
        addRow(c, 2, "E-mail:", email);
        addRow(c, 3, "Event:", event);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(register); buttons.add(clear);
        c.gridx = 1; c.gridy = 4;
        add(buttons, c);

        register.addActionListener(e -> onRegister());
        clear.addActionListener(e -> onClear());
        loadEvents();
    }

    private void addRow(GridBagConstraints c, int row, String label, java.awt.Component field) {
        c.gridx = 0; c.gridy = row; add(new JLabel(label), c);
        c.gridx = 1; add(field, c);
    }

    private void loadEvents() {
        new SwingWorker<java.util.List<Event>, Void>() {
            @Override protected java.util.List<Event> doInBackground() throws Exception { return service.events(); }
            @Override protected void done() {
                try {
                    for (Event e : get()) event.addItem(e);
                    event.setSelectedIndex(-1);
                } catch (ExecutionException ex) {
                    status.accept("Could not load events: " + ex.getCause().getMessage());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void onRegister() {
        Event selected = (Event) event.getSelectedItem();
        try {
            service.validate(studentId.getText(), name.getText(), email.getText(), selected);   // instant, on the EDT
        } catch (ValidationException ex) {
            status.accept("Invalid input: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        register.setEnabled(false);
        status.accept("Saving registration...");
        final String id = studentId.getText(), n = name.getText(), m = email.getText();

        new SwingWorker<Registration, Void>() {                       // database work off the EDT
            @Override protected Registration doInBackground() throws Exception {
                return service.register(id, n, m, selected);
            }
            @Override protected void done() {
                register.setEnabled(true);
                try {
                    Registration r = get();
                    status.accept("Registered " + r.getStudentId() + " for " + r.getEventName() + ".");
                    JOptionPane.showMessageDialog(RegistrationFormPanel.this,
                            "Registration successful.", "Done", JOptionPane.INFORMATION_MESSAGE);
                    onClear();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof DuplicateRegistrationException) {
                        status.accept("Duplicate registration rejected.");
                        JOptionPane.showMessageDialog(RegistrationFormPanel.this, cause.getMessage(),
                                "Already registered", JOptionPane.WARNING_MESSAGE);
                    } else if (cause instanceof ValidationException) {
                        status.accept("Invalid input: " + cause.getMessage());
                    } else if (cause instanceof DaoException) {
                        status.accept("Database error. Nothing was saved.");
                        JOptionPane.showMessageDialog(RegistrationFormPanel.this, cause.getMessage(),
                                "Database error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        status.accept("Unexpected error: " + cause);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void onClear() {
        studentId.setText(""); name.setText(""); email.setText("");
        event.setSelectedIndex(-1);
        studentId.requestFocusInWindow();
    }

    /** True while the Register button is disabled, i.e. a save is running (used when closing). */
    boolean isBusy() { return !register.isEnabled(); }
}

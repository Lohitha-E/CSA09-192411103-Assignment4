import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Task 2 - Hospital outpatient appointment desk.
 * Doctors are loaded ONCE into a Map<String, List<Doctor>>; changing the department
 * only swaps the contents of the doctor combo box. Every window event prints a trace
 * line, so the event sequence asked for in Task 2.4 can be captured from a real run.
 */
public class AppointmentDesk extends JFrame {

    static class Doctor {
        final String name;
        Doctor(String name) { this.name = name; }
        @Override public String toString() { return name; }
    }

    private final Map<String, List<Doctor>> doctorsByDepartment = new HashMap<>();
    private final JTextField patientId = new JTextField(10);
    private final JComboBox<String> department = new JComboBox<>();
    private final JComboBox<Doctor> doctor = new JComboBox<>();
    private final JComboBox<String> slot = new JComboBox<>(
            new String[]{"09:00", "09:30", "10:00", "10:30", "11:00", "11:30"});
    private final JButton book = new JButton("Book");
    private final JLabel status = new JLabel("Enter the patient ID and choose a department.");
    private final List<String> bookedKeys = new ArrayList<>();   // doctor|slot already taken
    private boolean bookingInProgress;                           // form started, not yet booked

    public AppointmentDesk() {
        super("Outpatient Appointment Desk");
        loadDoctorsOnce();

        JPanel form = new JPanel(new GridLayout(4, 2, 5, 5));
        form.add(new JLabel("Patient ID:"));  form.add(patientId);
        form.add(new JLabel("Department:"));  form.add(department);
        form.add(new JLabel("Doctor:"));      form.add(doctor);
        form.add(new JLabel("Time slot:"));   form.add(slot);
        JPanel south = new JPanel(new BorderLayout());
        JPanel buttonRow = new JPanel(new FlowLayout());
        buttonRow.add(book);
        south.add(buttonRow, BorderLayout.NORTH);
        south.add(status, BorderLayout.SOUTH);
        add(form, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        for (String d : doctorsByDepartment.keySet()) department.addItem(d);
        department.setSelectedIndex(-1);

        // (1) ItemListener on the department list -> refresh the doctors
        department.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) { trace("itemStateChanged(department)"); refreshDoctors(); }
        });
        // (2) ActionListener on the Book button
        book.addActionListener(e -> { trace("actionPerformed(Book)"); bookAppointment(); });
        // (3) DocumentListener-like behaviour kept simple: typing marks a booking as in progress
        patientId.addCaretListener(e -> bookingInProgress = !patientId.getText().isEmpty());

        // (4) WindowListener (through an adapter) - prints the whole window life cycle
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e)       { trace("windowOpened"); }
            @Override public void windowActivated(WindowEvent e)    { trace("windowActivated"); }
            @Override public void windowDeactivated(WindowEvent e)  { trace("windowDeactivated"); }
            @Override public void windowIconified(WindowEvent e)    { trace("windowIconified"); }
            @Override public void windowDeiconified(WindowEvent e)  { trace("windowDeiconified"); }
            @Override public void windowClosed(WindowEvent e)       { trace("windowClosed"); }
            @Override public void windowClosing(WindowEvent e) {
                trace("windowClosing");
                if (bookingInProgress) {
                    int c = JOptionPane.showConfirmDialog(AppointmentDesk.this,
                            "A booking is in progress. Close the window?", "Confirm close",
                            JOptionPane.YES_NO_OPTION);
                    if (c != JOptionPane.YES_OPTION) return;
                }
                dispose();
            }
        });
        trace("constructor finished");
        pack();
        setLocationRelativeTo(null);
    }

    private void loadDoctorsOnce() {
        doctorsByDepartment.put("Cardiology", List.of(new Doctor("Dr. Rao"), new Doctor("Dr. Mehta")));
        doctorsByDepartment.put("Orthopaedics", List.of(new Doctor("Dr. Iyer"), new Doctor("Dr. Khan"), new Doctor("Dr. Das")));
        doctorsByDepartment.put("Paediatrics", List.of(new Doctor("Dr. Nair")));
    }

    /** (a) Department changed: replace the doctor list from the map - no database call. */
    private void refreshDoctors() {
        doctor.removeAllItems();
        List<Doctor> list = doctorsByDepartment.get((String) department.getSelectedItem());
        if (list != null) for (Doctor d : list) doctor.addItem(d);
        status.setText(list == null ? "Choose a department." : list.size() + " doctor(s) available.");
    }

    /** (b) Validate, then book. Errors and confirmations are shown in the status label. */
    private void bookAppointment() {
        String id = patientId.getText().trim();
        if (!id.matches("\\d{6,10}")) { status.setText("Error: patient ID must be 6 to 10 digits."); return; }
        if (department.getSelectedIndex() < 0) { status.setText("Error: choose a department."); return; }
        if (doctor.getSelectedItem() == null) { status.setText("Error: choose a doctor."); return; }
        String key = doctor.getSelectedItem() + "|" + slot.getSelectedItem();
        if (bookedKeys.contains(key)) { status.setText("Error: " + doctor.getSelectedItem() + " is already booked at " + slot.getSelectedItem() + "."); return; }
        bookedKeys.add(key);
        bookingInProgress = false;
        status.setText("Booked: patient " + id + " with " + doctor.getSelectedItem() + " at " + slot.getSelectedItem() + ".");
    }

    private static void trace(String what) { System.out.println("[event] " + what); }

    public static void main(String[] args) {
        trace("main: before creating the window");
        SwingUtilities.invokeLater(() -> {
            AppointmentDesk w = new AppointmentDesk();
            trace("before setVisible(true)");
            w.setVisible(true);
            trace("after setVisible(true)");
        });
    }
}

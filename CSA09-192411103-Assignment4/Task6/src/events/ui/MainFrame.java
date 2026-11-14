package events.ui;

import events.service.RegistrationService;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

/**
 * Main window. Screen flow: "New Registration" form  <-->  "Registrations" table (cards),
 * plus the Export dialog (file chooser), the About dialog and the confirm-on-close dialog.
 */
public class MainFrame extends JFrame {
    private static final String FORM = "form", TABLE = "table";

    private final RegistrationService service;
    private final CardLayout cards = new CardLayout();
    private final JPanel deck = new JPanel(cards);
    private final JLabel status = new JLabel(" Ready.");
    private final RegistrationFormPanel form;
    private final RegistrationsPanel table;

    public MainFrame(RegistrationService service, String modeNote) {
        super("Campus Event Registration" + modeNote);
        this.service = service;
        form = new RegistrationFormPanel(service, status::setText);
        table = new RegistrationsPanel(service, status::setText);
        deck.add(form, FORM);
        deck.add(table, TABLE);
        add(deck, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        setJMenuBar(buildMenuBar());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { exit(); }
        });
        setSize(760, 460);
        setLocationRelativeTo(null);
    }

    private JMenuBar buildMenuBar() {
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        JMenu file = new JMenu("File");
        JMenuItem newReg = new JMenuItem("New Registration");
        newReg.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, mask));
        JMenuItem view = new JMenuItem("View Registrations");
        view.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, mask));
        JMenuItem export = new JMenuItem("Export CSV...");
        export.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, mask));
        JMenuItem exit = new JMenuItem("Exit");
        file.add(newReg); file.add(view); file.add(export); file.addSeparator(); file.add(exit);

        JMenu help = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        help.add(about);

        newReg.addActionListener(e -> cards.show(deck, FORM));
        view.addActionListener(e -> { cards.show(deck, TABLE); table.loadPage(); });
        export.addActionListener(e -> exportCsv());
        exit.addActionListener(e -> exit());
        about.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Campus Event Registration\nSwing + JDBC, layered: UI / service / DAO",
                "About", JOptionPane.INFORMATION_MESSAGE));

        JMenuBar bar = new JMenuBar();
        bar.add(file); bar.add(help);
        return bar;
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("registrations.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File target = chooser.getSelectedFile();
        int eventId = table.selectedEventId();                       // exports what the filter shows
        status.setText(" Exporting...");

        new SwingWorker<Integer, Void>() {
            @Override protected Integer doInBackground() throws Exception {
                try (Writer out = Files.newBufferedWriter(target.toPath(), StandardCharsets.UTF_8)) {
                    return service.exportCsv(eventId, out);
                }
            }
            @Override protected void done() {
                try {
                    int n = get();
                    status.setText(" Exported " + n + " row(s) to " + target.getName());
                    JOptionPane.showMessageDialog(MainFrame.this, "Exported " + n + " row(s) to\n" + target,
                            "Export complete", JOptionPane.INFORMATION_MESSAGE);
                } catch (ExecutionException ex) {
                    status.setText(" Export failed.");
                    JOptionPane.showMessageDialog(MainFrame.this, "Export failed: " + ex.getCause().getMessage(),
                            "Export", JOptionPane.ERROR_MESSAGE);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void exit() {
        if (form.isBusy()) {
            int c = JOptionPane.showConfirmDialog(this, "A registration is being saved. Exit anyway?",
                    "Confirm exit", JOptionPane.YES_NO_OPTION);
            if (c != JOptionPane.YES_OPTION) return;
        }
        dispose();
        System.exit(0);
    }
}

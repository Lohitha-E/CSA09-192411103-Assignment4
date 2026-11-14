package events.ui;

import events.model.Event;
import events.model.Registration;
import events.service.RegistrationService;
import events.service.RegistrationService.Page;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Table of registrations, filtered by event and loaded ONE PAGE AT A TIME (SQL LIMIT/OFFSET)
 * by a SwingWorker, so the window stays responsive however many rows the table holds.
 */
class RegistrationsPanel extends JPanel {
    private static final String[] COLUMNS = {"ID", "Student ID", "Name", "E-mail", "Event", "Registered at"};
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final RegistrationService service;
    private final Consumer<String> status;
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JComboBox<Event> filter = new JComboBox<>();
    private final JButton prev = new JButton("Prev");
    private final JButton next = new JButton("Next");
    private final JLabel pageLabel = new JLabel("Page 1 of 1");
    private int page;
    private int pageCount = 1;
    private boolean loading;

    RegistrationsPanel(RegistrationService service, Consumer<String> status) {
        this.service = service;
        this.status = status;
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Event:")); top.add(filter);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.add(prev); bottom.add(pageLabel); bottom.add(next);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        filter.addItem(Event.ALL);
        filter.addActionListener(e -> { page = 0; loadPage(); });
        prev.addActionListener(e -> { page--; loadPage(); });
        next.addActionListener(e -> { page++; loadPage(); });
        loadEvents();
    }

    int selectedEventId() { return ((Event) filter.getSelectedItem()).getId(); }

    private void loadEvents() {
        new SwingWorker<List<Event>, Void>() {
            @Override protected List<Event> doInBackground() throws Exception { return service.events(); }
            @Override protected void done() {
                try {
                    // "All events" is already selected, so adding items does not fire the filter listener
                    for (Event e : get()) filter.addItem(e);
                } catch (ExecutionException ex) {
                    status.accept("Could not load events: " + ex.getCause().getMessage());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    /** Refreshes the current page from the database. Safe to call at any time. */
    void loadPage() {
        if (loading) return;
        loading = true;
        setControlsEnabled(false);
        status.accept("Loading registrations...");
        final int eventId = selectedEventId(), requested = page;

        new SwingWorker<Page, Void>() {
            @Override protected Page doInBackground() throws Exception {
                return service.loadPage(eventId, requested);            // blocking query, off the EDT
            }
            @Override protected void done() {
                loading = false;
                try {
                    Page p = get();
                    model.setRowCount(0);
                    for (Registration r : p.rows) {
                        model.addRow(new Object[]{r.getId(), r.getStudentId(), r.getStudentName(), r.getEmail(),
                                r.getEventName(), r.getRegisteredAt().format(FORMAT)});
                    }
                    page = p.page;
                    pageCount = p.pageCount;
                    pageLabel.setText("Page " + (page + 1) + " of " + pageCount + "  (" + p.total + " rows)");
                    status.accept("Loaded " + p.rows.size() + " row(s).");
                } catch (ExecutionException ex) {
                    status.accept("Could not load registrations: " + ex.getCause().getMessage());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                setControlsEnabled(true);
            }
        }.execute();
    }

    private void setControlsEnabled(boolean enabled) {
        filter.setEnabled(enabled);
        prev.setEnabled(enabled && page > 0);
        next.setEnabled(enabled && page < pageCount - 1);
    }
}

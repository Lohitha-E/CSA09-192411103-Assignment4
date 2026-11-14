import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Task 3 - Campus library search screen.
 * Nested layout:
 *   JFrame (BorderLayout)
 *     NORTH  : topPanel  (BorderLayout)  -> NORTH title label, SOUTH searchRow (FlowLayout)
 *     CENTER : JScrollPane(JTextArea)    -> the only region that grows when the window is resized
 *     SOUTH  : bottomPanel (BorderLayout)-> NORTH buttonRow (FlowLayout), SOUTH status label
 */
public class LibrarySearch extends JFrame {
    private static final String[][] CATALOGUE = {
        {"Programming", "Core Java, Volume I"}, {"Programming", "Effective Java"},
        {"Programming", "Java Concurrency in Practice"}, {"Programming", "Head First Java"},
        {"Programming", "Clean Code"}, {"Databases", "Database System Concepts"},
        {"Databases", "SQL Antipatterns"}, {"Databases", "High Performance MySQL"},
        {"Networks", "Computer Networking: A Top-Down Approach"}, {"Maths", "Discrete Mathematics"},
    };
    private static final int PAGE_SIZE = 3;

    private final JTextField query = new JTextField(18);
    private final JComboBox<String> category =
            new JComboBox<>(new String[]{"All", "Programming", "Databases", "Networks", "Maths"});
    private final JButton search = new JButton("Search");
    private final JButton prev = new JButton("Prev");
    private final JButton next = new JButton("Next");
    private final JButton reserve = new JButton("Reserve");
    private final JTextArea results = new JTextArea();
    private final JLabel status = new JLabel("Ready.");

    private List<String> matches = new ArrayList<>();
    private int page;

    public LibrarySearch() {
        super("Campus Library Search");
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Campus Library", SwingConstants.CENTER);
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.CENTER));   // keeps natural size
        searchRow.add(new JLabel("Search:")); searchRow.add(query);
        searchRow.add(category); searchRow.add(search);
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(searchRow, BorderLayout.SOUTH);

        results.setEditable(false);
        JScrollPane centre = new JScrollPane(results);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER));   // keeps natural size
        buttonRow.add(prev); buttonRow.add(next); buttonRow.add(reserve);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonRow, BorderLayout.NORTH);
        bottomPanel.add(status, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(centre, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        search.addActionListener(e -> runSearch());
        query.addActionListener(e -> runSearch());
        prev.addActionListener(e -> { if (page > 0) { page--; showPage(); } });
        next.addActionListener(e -> { if ((page + 1) * PAGE_SIZE < matches.size()) { page++; showPage(); } });
        reserve.addActionListener(e -> reserveFirstOnPage());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(520, 360));
        pack();
        setLocationRelativeTo(null);
        showPage();
    }

    private void runSearch() {
        String text = query.getText().trim().toLowerCase();
        String cat = (String) category.getSelectedItem();
        matches = new ArrayList<>();
        for (String[] row : CATALOGUE) {
            boolean catOk = cat.equals("All") || row[0].equals(cat);
            if (catOk && row[1].toLowerCase().contains(text)) matches.add(row[1] + "  [" + row[0] + "]");
        }
        page = 0;
        showPage();
        status.setText(matches.size() + " result(s) found.");
    }

    private void showPage() {
        StringBuilder sb = new StringBuilder();
        int from = page * PAGE_SIZE, to = Math.min(from + PAGE_SIZE, matches.size());
        for (int i = from; i < to; i++) sb.append(i + 1).append(". ").append(matches.get(i)).append("\n");
        results.setText(sb.length() == 0 ? "No results. Type a title and press Search." : sb.toString());
        prev.setEnabled(page > 0);
        next.setEnabled(to < matches.size());
        reserve.setEnabled(to > from);
    }

    private void reserveFirstOnPage() {
        int idx = page * PAGE_SIZE;
        if (idx < matches.size()) status.setText("Reserved: " + matches.get(idx));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibrarySearch().setVisible(true));
    }
}

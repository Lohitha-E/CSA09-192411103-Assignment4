package events;

import events.dao.InMemoryRegistrationDao;
import events.dao.JdbcRegistrationDao;
import events.dao.RegistrationDao;
import events.service.RegistrationService;
import events.ui.MainFrame;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.swing.SwingUtilities;

/**
 * Entry point. If db.properties exists (db.url, db.user, db.password) the application uses
 * MySQL through JDBC; otherwise it starts in demo mode with an in-memory DAO.
 */
public class App {
    public static void main(String[] args) {
        String path = args.length > 0 ? args[0] : "db.properties";
        RegistrationDao dao;
        String mode;
        Properties p = new Properties();
        try (InputStream in = new FileInputStream(path)) {
            p.load(in);
            dao = new JdbcRegistrationDao(p.getProperty("db.url"), p.getProperty("db.user"), p.getProperty("db.password"));
            mode = " [MySQL]";
        } catch (IOException e) {
            InMemoryRegistrationDao demo = new InMemoryRegistrationDao();
            demo.seed(45);                                            // three pages of sample rows
            dao = demo;
            mode = " [demo mode - no database]";
            System.out.println("db.properties not found: starting in demo mode.");
        }
        RegistrationService service = new RegistrationService(dao);
        String note = mode;
        SwingUtilities.invokeLater(() -> new MainFrame(service, note).setVisible(true));
    }
}

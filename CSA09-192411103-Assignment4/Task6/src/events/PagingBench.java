package events;

import events.dao.InMemoryRegistrationDao;
import events.model.Registration;
import events.service.RegistrationService;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 * Task 6 - Alternative Concept: all 100,000 registrations in the table at once versus one
 * page of 20 rows. This version uses the in-memory DAO, so it measures the Java side only
 * (objects and table-model rows). For the MySQL side run seed_100k.sql and time the two
 * queries (SELECT ... without LIMIT versus LIMIT 20 OFFSET n) in your own database.
 */
public class PagingBench {
    static long usedMb() {
        System.gc();
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
    }

    public static void main(String[] args) throws Exception {
        final int n = 100_000;
        InMemoryRegistrationDao dao = new InMemoryRegistrationDao();
        dao.seed(n);
        RegistrationService service = new RegistrationService(dao);
        String[] cols = {"ID", "Student ID", "Name", "E-mail", "Event", "Registered at"};

        long base = usedMb();   // the DAO already holds its own 100,000 rows; only the table copy is measured
        long t0 = System.nanoTime();
        List<Registration> all = dao.findPage(0, n, 0);
        DefaultTableModel big = new DefaultTableModel(cols, 0);
        for (Registration r : all) {
            big.addRow(new Object[]{r.getId(), r.getStudentId(), r.getStudentName(), r.getEmail(),
                    r.getEventName(), r.getRegisteredAt().toString()});
        }
        long t1 = System.nanoTime();
        long afterAll = usedMb();
        System.out.println("Rows held in the full table model: " + big.getRowCount() + " (kept alive until here)");

        all = null; big = null;
        long t2 = System.nanoTime();
        RegistrationService.Page page = service.loadPage(0, 2500);       // a page deep in the data
        DefaultTableModel small = new DefaultTableModel(cols, 0);
        for (Registration r : page.rows) {
            small.addRow(new Object[]{r.getId(), r.getStudentId(), r.getStudentName(), r.getEmail(),
                    r.getEventName(), r.getRegisteredAt().toString()});
        }
        long t3 = System.nanoTime();

        System.out.printf("Load all %,d rows into the table model: %.1f ms%n", n, (t1 - t0) / 1e6);
        System.out.printf("Extra heap held by the full table: about %d MB (heap before %d MB, after %d MB)%n",
                afterAll - base, base, afterAll);
        System.out.printf("Load one page (%d rows, page 2501): %.3f ms%n", page.rows.size(), (t3 - t2) / 1e6);
    }
}

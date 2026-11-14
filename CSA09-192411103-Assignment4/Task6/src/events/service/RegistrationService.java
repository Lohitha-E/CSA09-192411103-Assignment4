package events.service;

import events.dao.RegistrationDao;
import events.dao.RegistrationDao.DaoException;
import events.dao.RegistrationDao.DuplicateRegistrationException;
import events.model.Event;
import events.model.Registration;
import java.io.IOException;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Application rules. No Swing imports: validation, the duplicate rule, paging and export
 * can be tested without a window and without a database (see ServiceTest).
 */
public class RegistrationService {

    /** Input the user can fix; the message is shown to the user as it is. */
    public static class ValidationException extends Exception {
        public ValidationException(String message) { super(message); }
    }

    /** One page of rows plus what the Prev/Next buttons need to know. */
    public static final class Page {
        public final List<Registration> rows;
        public final int total, page, pageCount;
        Page(List<Registration> rows, int total, int page, int pageCount) {
            this.rows = rows; this.total = total; this.page = page; this.pageCount = pageCount;
        }
    }

    public static final int PAGE_SIZE = 20;
    private static final int EXPORT_CHUNK = 500;

    private static final Pattern STUDENT_ID = Pattern.compile("\\d{9}");
    private static final Pattern NAME = Pattern.compile("[A-Za-z][A-Za-z .'-]{1,59}");
    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");

    private final RegistrationDao dao;

    public RegistrationService(RegistrationDao dao) { this.dao = dao; }

    public List<Event> events() throws DaoException { return dao.findEvents(); }

    /** Pure validation; fast, so it can run on the event-dispatch thread for instant feedback. */
    public void validate(String studentId, String name, String email, Event event) throws ValidationException {
        if (studentId == null || !STUDENT_ID.matcher(studentId.trim()).matches())
            throw new ValidationException("Student ID must be exactly 9 digits.");
        if (name == null || !NAME.matcher(name.trim()).matches())
            throw new ValidationException("Enter a valid name (2 to 60 letters).");
        if (email == null || !EMAIL.matcher(email.trim()).matches())
            throw new ValidationException("Enter a valid e-mail address.");
        if (event == null || event.getId() == 0)
            throw new ValidationException("Select an event.");
    }

    /** Blocking (database access): call from a worker thread. */
    public Registration register(String studentId, String name, String email, Event event)
            throws ValidationException, DuplicateRegistrationException, DaoException {
        validate(studentId, name, email, event);
        String id = studentId.trim();
        if (dao.exists(id, event.getId()))                       // friendly early check
            throw new DuplicateRegistrationException("This student is already registered for " + event.getName() + ".");
        Registration r = Registration.create(id, name.trim(), email.trim(), event);
        dao.insert(r);                                           // the UNIQUE constraint still protects against races
        return r;
    }

    /** Blocking: loads one page (SQL LIMIT/OFFSET) and the total row count. */
    public Page loadPage(int eventId, int requestedPage) throws DaoException {
        int total = dao.count(eventId);
        int pageCount = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = Math.min(Math.max(0, requestedPage), pageCount - 1);
        return new Page(dao.findPage(eventId, PAGE_SIZE, page * PAGE_SIZE), total, page, pageCount);
    }

    /** Blocking: writes every registration of the event as CSV, reading the table in chunks. */
    public int exportCsv(int eventId, Writer out) throws DaoException, IOException {
        out.write("id,student_id,student_name,email,event,registered_at\n");
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        int written = 0;
        while (true) {
            List<Registration> chunk = dao.findPage(eventId, EXPORT_CHUNK, written);
            if (chunk.isEmpty()) break;
            for (Registration r : chunk) {
                out.write(r.getId() + "," + csv(r.getStudentId()) + "," + csv(r.getStudentName()) + ","
                        + csv(r.getEmail()) + "," + csv(r.getEventName()) + "," + r.getRegisteredAt().format(f) + "\n");
            }
            written += chunk.size();
        }
        out.flush();
        return written;
    }

    private static String csv(String s) {
        return (s.contains(",") || s.contains("\"") || s.contains("\n")) ? "\"" + s.replace("\"", "\"\"") + "\"" : s;
    }
}

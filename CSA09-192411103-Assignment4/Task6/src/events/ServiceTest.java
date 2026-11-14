package events;

import events.dao.InMemoryRegistrationDao;
import events.dao.RegistrationDao.DuplicateRegistrationException;
import events.model.Event;
import events.service.RegistrationService;
import events.service.RegistrationService.Page;
import events.service.RegistrationService.ValidationException;
import java.io.StringWriter;

/** Plain-Java tests for the service layer: no window, no database needed. */
public class ServiceTest {
    static int failures = 0;

    static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (!ok) failures++;
    }

    static boolean invalid(RegistrationService s, String id, String name, String email, Event e) {
        try { s.validate(id, name, email, e); return false; }
        catch (ValidationException ex) { return true; }
    }

    public static void main(String[] args) throws Exception {
        InMemoryRegistrationDao dao = new InMemoryRegistrationDao();
        RegistrationService s = new RegistrationService(dao);
        Event cultural = s.events().get(0), dance = s.events().get(1);

        // normal registration
        s.register("192411103", "E Lohitha", "lohitha@example.edu", cultural);
        check("normal registration stored", dao.count(0) == 1 && dao.exists("192411103", cultural.getId()));

        // invalid input
        check("short student id rejected", invalid(s, "1924", "E Lohitha", "a@b.co", cultural));
        check("letters in student id rejected", invalid(s, "19241110A", "E Lohitha", "a@b.co", cultural));
        check("empty name rejected", invalid(s, "192411104", "", "a@b.co", cultural));
        check("digits in name rejected", invalid(s, "192411104", "R2D2", "a@b.co", cultural));
        check("bad e-mail rejected", invalid(s, "192411104", "Arun", "not-an-email", cultural));
        check("no event selected rejected", invalid(s, "192411104", "Arun", "a@b.co", null));
        check("'All events' is not a valid choice", invalid(s, "192411104", "Arun", "a@b.co", Event.ALL));
        check("invalid input stored nothing", dao.count(0) == 1);

        // duplicate registration: same student, same event
        try { s.register("192411103", "E Lohitha", "lohitha@example.edu", cultural); check("duplicate rejected", false); }
        catch (DuplicateRegistrationException e) { check("duplicate rejected: " + e.getMessage(), true); }
        check("duplicate stored nothing", dao.count(0) == 1);

        // same student, different event is allowed
        s.register("192411103", "E Lohitha", "lohitha@example.edu", dance);
        check("same student, other event allowed", dao.count(0) == 2);

        // the DAO-level unique rule also catches a race that slips past the early check
        try { dao.insert(events.model.Registration.create("192411103", "E Lohitha", "x@y.co", dance)); check("DAO unique rule", false); }
        catch (DuplicateRegistrationException e) { check("DAO unique rule rejects a direct duplicate insert", true); }

        // paging: 45 more rows -> 47 in total, pages of 20
        dao.seed(45);
        Page p0 = s.loadPage(0, 0), p2 = s.loadPage(0, 2), pBeyond = s.loadPage(0, 99);
        check("total rows = 47", p0.total == 47);
        check("page count = 3", p0.pageCount == 3);
        check("first page has 20 rows", p0.rows.size() == 20);
        check("last page has 7 rows", p2.rows.size() == 7);
        check("page number beyond the end is clamped to the last page", pBeyond.page == 2);
        Page filtered = s.loadPage(cultural.getId(), 0);
        check("filter by event returns only that event", filtered.rows.stream().allMatch(r -> r.getEventId() == cultural.getId()));

        // CSV export (rows are read in chunks of 500)
        StringWriter out = new StringWriter();
        int n = s.exportCsv(0, out);
        String[] lines = out.toString().split("\n");
        check("export wrote every row", n == 47 && lines.length == 48);
        check("export has a header line", lines[0].startsWith("id,student_id,student_name"));

        System.out.println(failures == 0 ? "All tests passed." : failures + " test(s) failed.");
    }
}

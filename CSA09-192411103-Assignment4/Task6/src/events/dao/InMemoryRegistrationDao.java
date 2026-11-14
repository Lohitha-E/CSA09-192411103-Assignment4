package events.dao;

import events.model.Event;
import events.model.Registration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Demo / test implementation that behaves like the MySQL one, including the unique rule
 * (student, event). It lets the application run and be tested without a database.
 */
public class InMemoryRegistrationDao implements RegistrationDao {
    private final List<Event> events = List.of(
            new Event(1, "Cultural Night"), new Event(2, "Dance Competition"),
            new Event(3, "Music Fest"), new Event(4, "Drama Evening"), new Event(5, "Art Exhibition"));
    private final List<Registration> rows = new ArrayList<>();
    private final Set<String> keys = new HashSet<>();
    private long nextId = 1;

    @Override public List<Event> findEvents() { return events; }

    @Override public synchronized boolean exists(String studentId, int eventId) {
        return keys.contains(studentId + "|" + eventId);
    }

    @Override
    public synchronized void insert(Registration r) throws DuplicateRegistrationException {
        if (!keys.add(r.getStudentId() + "|" + r.getEventId())) {
            throw new DuplicateRegistrationException("This student is already registered for this event.");
        }
        rows.add(new Registration(nextId++, r.getStudentId(), r.getStudentName(), r.getEmail(),
                r.getEventId(), r.getEventName(), r.getRegisteredAt()));
    }

    @Override
    public synchronized List<Registration> findPage(int eventId, int limit, int offset) {
        List<Registration> page = new ArrayList<>();
        int skipped = 0;
        for (Registration r : rows) {
            if (eventId != 0 && r.getEventId() != eventId) continue;
            if (skipped++ < offset) continue;
            if (page.size() == limit) break;
            page.add(r);
        }
        return page;
    }

    @Override
    public synchronized int count(int eventId) {
        if (eventId == 0) return rows.size();
        int n = 0;
        for (Registration r : rows) if (r.getEventId() == eventId) n++;
        return n;
    }

    /** Adds n sample rows spread over the five events (for demonstrations and measurements). */
    public synchronized void seed(int n) {
        for (int i = 1; i <= n; i++) {
            Event e = events.get(i % 5);
            String id = String.format("%09d", i);
            keys.add(id + "|" + e.getId());
            rows.add(new Registration(nextId++, id, "Student " + i, "student" + i + "@example.edu",
                    e.getId(), e.getName(), LocalDateTime.now()));
        }
    }
}

package events.dao;

import events.model.Event;
import events.model.Registration;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * MySQL implementation. Every statement uses placeholders (no string concatenation) and
 * every connection, statement and result set is closed by try-with-resources.
 * Methods block, so callers must run them off the event-dispatch thread.
 */
public class JdbcRegistrationDao implements RegistrationDao {
    private static final int MYSQL_DUPLICATE_ENTRY = 1062;

    private final String url;
    private final String user;
    private final String password;

    public JdbcRegistrationDao(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection open() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public List<Event> findEvents() throws DaoException {
        String sql = "SELECT id, name FROM events ORDER BY event_date, id";
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Event> list = new ArrayList<>();
            while (rs.next()) list.add(new Event(rs.getInt(1), rs.getString(2)));
            return list;
        } catch (SQLException e) {
            throw new DaoException("Could not load the events.", e);
        }
    }

    @Override
    public boolean exists(String studentId, int eventId) throws DaoException {
        String sql = "SELECT 1 FROM registrations WHERE student_id = ? AND event_id = ? LIMIT 1";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DaoException("Could not check for an existing registration.", e);
        }
    }

    @Override
    public void insert(Registration r) throws DuplicateRegistrationException, DaoException {
        String sql = "INSERT INTO registrations (student_id, student_name, email, event_id) VALUES (?, ?, ?, ?)";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getStudentId());
            ps.setString(2, r.getStudentName());
            ps.setString(3, r.getEmail());
            ps.setInt(4, r.getEventId());
            ps.executeUpdate();
        } catch (SQLException e) {
            // The UNIQUE (student_id, event_id) constraint is the real guarantee against duplicates.
            if (e.getErrorCode() == MYSQL_DUPLICATE_ENTRY) {
                throw new DuplicateRegistrationException("This student is already registered for this event.");
            }
            throw new DaoException("The registration could not be saved.", e);
        }
    }

    @Override
    public List<Registration> findPage(int eventId, int limit, int offset) throws DaoException {
        String sql = "SELECT r.id, r.student_id, r.student_name, r.email, r.event_id, e.name, r.registered_at "
                   + "FROM registrations r JOIN events e ON e.id = r.event_id "
                   + (eventId == 0 ? "" : "WHERE r.event_id = ? ")
                   + "ORDER BY r.id LIMIT ? OFFSET ?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            if (eventId != 0) ps.setInt(i++, eventId);
            ps.setInt(i++, limit);
            ps.setInt(i, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Registration> page = new ArrayList<>(limit);
                while (rs.next()) {
                    page.add(new Registration(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4),
                            rs.getInt(5), rs.getString(6), rs.getTimestamp(7).toLocalDateTime()));
                }
                return page;
            }
        } catch (SQLException e) {
            throw new DaoException("Could not load the registrations.", e);
        }
    }

    @Override
    public int count(int eventId) throws DaoException {
        String sql = "SELECT COUNT(*) FROM registrations" + (eventId == 0 ? "" : " WHERE event_id = ?");
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (eventId != 0) ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DaoException("Could not count the registrations.", e);
        }
    }
}

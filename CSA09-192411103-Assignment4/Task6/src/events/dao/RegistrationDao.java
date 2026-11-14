package events.dao;

import events.model.Event;
import events.model.Registration;
import java.util.List;

/**
 * Data-access contract. The service layer depends only on this interface, so the JDBC
 * implementation can be replaced by an in-memory one for tests and demonstrations.
 */
public interface RegistrationDao {

    /** Any storage failure (connection lost, SQL error). The original exception is the cause. */
    class DaoException extends Exception {
        public DaoException(String message, Throwable cause) { super(message, cause); }
    }

    /** The student is already registered for the event (the unique rule was violated). */
    class DuplicateRegistrationException extends Exception {
        public DuplicateRegistrationException(String message) { super(message); }
    }

    List<Event> findEvents() throws DaoException;

    boolean exists(String studentId, int eventId) throws DaoException;

    void insert(Registration registration) throws DuplicateRegistrationException, DaoException;

    /** One page of rows, newest id last. eventId 0 means all events. */
    List<Registration> findPage(int eventId, int limit, int offset) throws DaoException;

    int count(int eventId) throws DaoException;
}

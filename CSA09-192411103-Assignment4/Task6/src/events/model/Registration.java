package events.model;

import java.time.LocalDateTime;

/** One row of the registrations table (immutable). */
public final class Registration {
    private final long id;
    private final String studentId;
    private final String studentName;
    private final String email;
    private final int eventId;
    private final String eventName;
    private final LocalDateTime registeredAt;

    public Registration(long id, String studentId, String studentName, String email,
                        int eventId, String eventName, LocalDateTime registeredAt) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.email = email;
        this.eventId = eventId;
        this.eventName = eventName;
        this.registeredAt = registeredAt;
    }

    /** A new registration that has not been stored yet. */
    public static Registration create(String studentId, String studentName, String email, Event event) {
        return new Registration(0, studentId, studentName, email, event.getId(), event.getName(), LocalDateTime.now());
    }

    public long getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getEmail() { return email; }
    public int getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
}

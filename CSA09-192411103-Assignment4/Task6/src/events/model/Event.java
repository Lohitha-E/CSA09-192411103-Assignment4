package events.model;

/** An event students can register for. id 0 is reserved for the "All events" filter item. */
public final class Event {
    public static final Event ALL = new Event(0, "All events");

    private final int id;
    private final String name;

    public Event(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    @Override public String toString() { return name; }   // used by JComboBox
}

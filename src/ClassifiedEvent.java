public class ClassifiedEvent {
    private final Event event;
    private final String category;
    private final String destination;
    private final boolean shutdownSignal;

    public ClassifiedEvent(Event event, String category, String destination) {
        this(event, category, destination, false);
    }

    private ClassifiedEvent(Event event, String category, String destination, boolean shutdownSignal) {
        this.event = event;
        this.category = category;
        this.destination = destination;
        this.shutdownSignal = shutdownSignal;
    }

    public static ClassifiedEvent shutdownSignal() {
        return new ClassifiedEvent(Event.shutdownSignal(), "SHUTDOWN", "NONE", true);
    }

    public Event getEvent() {
        return event;
    }

    public String getCategory() {
        return category;
    }

    public String getDestination() {
        return destination;
    }

    public boolean isShutdownSignal() {
        return shutdownSignal;
    }

    @Override
    public String toString() {
        if (shutdownSignal) {
            return "SHUTDOWN";
        }

        return String.format(
            "%s -> category=%s, destination=%s",
            event,
            category,
            destination
        );
    }
}

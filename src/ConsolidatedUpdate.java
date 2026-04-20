public class ConsolidatedUpdate {
    private final Event event;
    private final String status;
    private final boolean shutdownSignal;

    public ConsolidatedUpdate(Event event, String status) {
        this(event, status, false);
    }

    private ConsolidatedUpdate(Event event, String status, boolean shutdownSignal) {
        this.event = event;
        this.status = status;
        this.shutdownSignal = shutdownSignal;
    }

    public static ConsolidatedUpdate shutdownSignal() {
        return new ConsolidatedUpdate(Event.shutdownSignal(), "SHUTDOWN", true);
    }

    public Event getEvent() {
        return event;
    }

    public String getStatus() {
        return status;
    }

    public boolean isShutdownSignal() {
        return shutdownSignal;
    }
}

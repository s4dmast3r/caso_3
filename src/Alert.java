public class Alert {
    private final Event event;
    private final String reason;
    private final long createdAt;
    private final boolean shutdownSignal;

    public Alert(Event event, String reason) {
        this(event, reason, System.currentTimeMillis(), false);
    }

    private Alert(Event event, String reason, long createdAt, boolean shutdownSignal) {
        this.event = event;
        this.reason = reason;
        this.createdAt = createdAt;
        this.shutdownSignal = shutdownSignal;
    }

    public static Alert shutdownSignal() {
        return new Alert(Event.shutdownSignal(), "SHUTDOWN", System.currentTimeMillis(), true);
    }

    public Event getEvent() {
        return event;
    }

    public String getReason() {
        return reason;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isShutdownSignal() {
        return shutdownSignal;
    }
}

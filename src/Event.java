public class Event {
    private final String eventId;
    private final int sensorId;
    private final EventType type;
    private final EventPriority priority;
    private final double value;
    private final long timestamp;
    private int targetServerId;
    private boolean shutdownSignal;

    public Event(
        String eventId,
        int sensorId,
        EventType type,
        EventPriority priority,
        double value,
        long timestamp
    ) {
        this.eventId = eventId;
        this.sensorId = sensorId;
        this.type = type;
        this.priority = priority;
        this.value = value;
        this.timestamp = timestamp;
    }

    public static Event shutdownSignal() {
        Event signal = new Event(
            "SHUTDOWN",
            0,
            EventType.SHUTDOWN,
            EventPriority.LOW,
            0.0,
            System.currentTimeMillis()
        );
        signal.shutdownSignal = true;
        return signal;
    }

    public String getEventId() {
        return eventId;
    }

    public int getSensorId() {
        return sensorId;
    }

    public EventType getType() {
        return type;
    }

    public EventPriority getPriority() {
        return priority;
    }

    public double getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTargetServerId(int targetServerId) {
        this.targetServerId = targetServerId;
    }

    public int getTargetServerId() {
        return targetServerId;
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
            "%s [sensor=%d, type=%s, priority=%s, value=%.2f, targetServer=%d]",
            eventId,
            sensorId,
            type,
            priority,
            value,
            targetServerId
        );
    }
}

public class Event {
    private final String eventId;
    private final int sensorId;
    private final int sequence;
    private final int eventType;
    private final long createdAt;
    private final boolean finishSignal;
    private final String finishSource;

    private Event(
        String eventId,
        int sensorId,
        int sequence,
        int eventType,
        long createdAt,
        boolean finishSignal,
        String finishSource
    ) {
        this.eventId = eventId;
        this.sensorId = sensorId;
        this.sequence = sequence;
        this.eventType = eventType;
        this.createdAt = createdAt;
        this.finishSignal = finishSignal;
        this.finishSource = finishSource;
    }

    public static Event sensorEvent(int sensorId, int sequence, int eventType) {
        String eventId = String.format("S%02d-E%04d", sensorId, sequence);
        return new Event(eventId, sensorId, sequence, eventType, System.currentTimeMillis(), false, "");
    }

    public static Event finishSignal(String finishSource) {
        return new Event("FIN-" + finishSource, 0, 0, 0, System.currentTimeMillis(), true, finishSource);
    }

    public String getEventId() {
        return eventId;
    }

    public int getSensorId() {
        return sensorId;
    }

    public int getSequence() {
        return sequence;
    }

    public int getEventType() {
        return eventType;
    }

    public int getTargetServerId() {
        return eventType;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isFinishSignal() {
        return finishSignal;
    }

    public String getFinishSource() {
        return finishSource;
    }

    public String getTypeLabel() {
        return finishSignal ? "FIN" : "TYPE_" + eventType;
    }

    @Override
    public String toString() {
        if (finishSignal) {
            return "FIN(" + finishSource + ")";
        }

        return String.format(
            "%s [sensor=%d, seq=%d, type=%s, targetServer=%d]",
            eventId,
            sensorId,
            sequence,
            getTypeLabel(),
            getTargetServerId()
        );
    }
}

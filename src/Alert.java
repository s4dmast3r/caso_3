public class Alert {
    private final Event event;
    private final int brokerRandomValue;
    private final long createdAt;
    private final boolean finishSignal;

    public Alert(Event event, int brokerRandomValue) {
        this(event, brokerRandomValue, System.currentTimeMillis(), false);
    }

    private Alert(Event event, int brokerRandomValue, long createdAt, boolean finishSignal) {
        this.event = event;
        this.brokerRandomValue = brokerRandomValue;
        this.createdAt = createdAt;
        this.finishSignal = finishSignal;
    }

    public static Alert finishSignal() {
        return new Alert(Event.finishSignal("BROKER_A_ADMIN"), -1, System.currentTimeMillis(), true);
    }

    public Event getEvent() {
        return event;
    }

    public int getBrokerRandomValue() {
        return brokerRandomValue;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isFinishSignal() {
        return finishSignal;
    }
}

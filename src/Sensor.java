import java.util.concurrent.ThreadLocalRandom;

public class Sensor extends Thread {
    private final int sensorId;
    private final EventQueue eventInbox;
    private final int numEventsPerSensor;
    private final int numServers;

    public Sensor(int sensorId, EventQueue eventInbox, int numEventsPerSensor, int numServers) {
        super("Sensor-" + sensorId);
        this.sensorId = sensorId;
        this.eventInbox = eventInbox;
        this.numEventsPerSensor = numEventsPerSensor;
        this.numServers = numServers;
    }

    @Override
    public void run() {
        try {
            System.out.printf("[%s] Iniciando generacion de %d eventos.%n", getName(), numEventsPerSensor);

            for (int i = 1; i <= numEventsPerSensor; i++) {
                Event event = createEvent(i);
                eventInbox.put(event);
                System.out.printf(
                    "[%s] Generado evento %s (destino servidor %d).%n",
                    getName(),
                    event.getEventId(),
                    event.getTargetServerId()
                );
                Thread.sleep(ThreadLocalRandom.current().nextInt(10, 50));
            }

            System.out.printf("[%s] Finalizo generacion de eventos.%n", getName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.printf("[%s] Interrumpido durante generacion.%n", getName());
        }
    }

    private Event createEvent(int sequence) {
        int targetServerId = ThreadLocalRandom.current().nextInt(1, numServers + 1);
        EventType type = deriveEventType(targetServerId);
        double value = generateValueForType(type);
        EventPriority priority = calculatePriority(type, value);
        String eventId = String.format("EVT-S%02d-%03d", sensorId, sequence);
        Event event = new Event(eventId, sensorId, type, priority, value, System.currentTimeMillis());
        event.setTargetServerId(targetServerId);
        return event;
    }

    private EventType deriveEventType(int serverId) {
        switch (serverId) {
            case 1:
                return EventType.TEMPERATURE;
            case 2:
                return EventType.HUMIDITY;
            case 3:
                return EventType.PRESSURE;
            case 4:
                return EventType.MOTION;
            case 5:
                return EventType.SMOKE;
            default:
                return EventType.TEMPERATURE;
        }
    }

    private double generateValueForType(EventType type) {
        switch (type) {
            case TEMPERATURE:
                return ThreadLocalRandom.current().nextDouble(15.0, 95.0);
            case HUMIDITY:
                return ThreadLocalRandom.current().nextDouble(20.0, 100.0);
            case PRESSURE:
                return ThreadLocalRandom.current().nextDouble(850.0, 1200.0);
            case MOTION:
                return ThreadLocalRandom.current().nextDouble(0.0, 10.0);
            case SMOKE:
                return ThreadLocalRandom.current().nextDouble(0.0, 300.0);
            default:
                return 0.0;
        }
    }

    private EventPriority calculatePriority(EventType type, double value) {
        switch (type) {
            case TEMPERATURE:
                if (value >= 80.0) {
                    return EventPriority.CRITICAL;
                }
                if (value >= 65.0) {
                    return EventPriority.HIGH;
                }
                return EventPriority.MEDIUM;
            case HUMIDITY:
                if (value >= 90.0) {
                    return EventPriority.HIGH;
                }
                return EventPriority.MEDIUM;
            case PRESSURE:
                if (value >= 1120.0 || value <= 900.0) {
                    return EventPriority.HIGH;
                }
                return EventPriority.MEDIUM;
            case MOTION:
                if (value >= 8.0) {
                    return EventPriority.HIGH;
                }
                return EventPriority.LOW;
            case SMOKE:
                if (value >= 220.0) {
                    return EventPriority.CRITICAL;
                }
                if (value >= 140.0) {
                    return EventPriority.HIGH;
                }
                return EventPriority.MEDIUM;
            default:
                return EventPriority.LOW;
        }
    }
}

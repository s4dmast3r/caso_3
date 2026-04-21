import java.util.Random;

public class Sensor extends Thread {
    private final int sensorId;
    private final int eventsToGenerate;
    private final int numServers;
    private final PassiveMailbox<Event> inputMailbox;
    private final Random random;
    private int generatedCount;

    public Sensor(int sensorId, int eventsToGenerate, int numServers, PassiveMailbox<Event> inputMailbox) {
        super("Sensor-" + sensorId);
        this.sensorId = sensorId;
        this.eventsToGenerate = eventsToGenerate;
        this.numServers = numServers;
        this.inputMailbox = inputMailbox;
        this.random = new Random(System.nanoTime() + sensorId);
    }

    @Override
    public void run() {
        System.out.printf("[%s] Inicio. Generara %d eventos.%n", getName(), eventsToGenerate);

        for (int sequence = 1; sequence <= eventsToGenerate; sequence++) {
            int eventType = random.nextInt(numServers) + 1;
            Event event = Event.sensorEvent(sensorId, sequence, eventType);
            inputMailbox.put(event);
            generatedCount++;
            System.out.printf(
                "[%s] Generado %s en %s.%n",
                getName(),
                event,
                inputMailbox.getName()
            );
        }

        System.out.printf("[%s] Fin. Eventos generados=%d.%n", getName(), generatedCount);
    }

    public int getGeneratedCount() {
        return generatedCount;
    }
}

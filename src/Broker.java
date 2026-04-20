import java.util.concurrent.ThreadLocalRandom;

public class Broker extends Thread {
    private final EventQueue inputQueue;
    private final BlockingMailbox<Alert> alertQueue;
    private final EventQueue classifierQueue;
    private final int totalEventsToProcess;

    public Broker(
        EventQueue inputQueue,
        BlockingMailbox<Alert> alertQueue,
        EventQueue classifierQueue,
        int totalEventsToProcess
    ) {
        super("Broker");
        this.inputQueue = inputQueue;
        this.alertQueue = alertQueue;
        this.classifierQueue = classifierQueue;
        this.totalEventsToProcess = totalEventsToProcess;
    }

    @Override
    public void run() {
        try {
            System.out.printf("[%s] Iniciando procesamiento de %d eventos.%n", getName(), totalEventsToProcess);

            int anomalousCount = 0;
            int validCount = 0;

            for (int i = 0; i < totalEventsToProcess; i++) {
                Event event = inputQueue.take();
                if (isAnomalous()) {
                    anomalousCount++;
                    alertQueue.put(new Alert(event, "Evento detectado como anomalo"));
                    System.out.printf("[%s] Evento ANOMALO enviado a administrador: %s%n", getName(), event.getEventId());
                } else {
                    validCount++;
                    classifierQueue.put(event);
                    System.out.printf("[%s] Evento VALIDO enviado a clasificacion: %s%n", getName(), event.getEventId());
                }
            }

            System.out.printf("[%s] Procesados: %d validos, %d anomalos.%n", getName(), validCount, anomalousCount);
            alertQueue.put(Alert.shutdownSignal());
            System.out.printf("[%s] Evento de fin enviado a administrador.%n", getName());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.printf("[%s] Interrumpido.%n", getName());
        }
    }

    private boolean isAnomalous() {
        int randomValue = ThreadLocalRandom.current().nextInt(0, 201);
        boolean anomaly = randomValue % 8 == 0;
        System.out.printf("[%s] Valor aleatorio broker=%d, anomalo=%b%n", getName(), randomValue, anomaly);
        return anomaly;
    }
}

import java.util.Random;

public class Broker extends Thread {
    private final PassiveMailbox<Event> inputMailbox;
    private final PassiveMailbox<Alert> alertMailbox;
    private final LimitedMailbox<Event> classificationMailbox;
    private final int totalEventsExpected;
    private final Random random;

    private int processedCount;
    private int normalCount;
    private int suspiciousCount;

    public Broker(
        PassiveMailbox<Event> inputMailbox,
        PassiveMailbox<Alert> alertMailbox,
        LimitedMailbox<Event> classificationMailbox,
        int totalEventsExpected
    ) {
        super("Broker-Analizador");
        this.inputMailbox = inputMailbox;
        this.alertMailbox = alertMailbox;
        this.classificationMailbox = classificationMailbox;
        this.totalEventsExpected = totalEventsExpected;
        this.random = new Random(System.nanoTime());
    }

    @Override
    public void run() {
        try {
            System.out.printf("[%s] Inicio. Debe procesar %d eventos.%n", getName(), totalEventsExpected);

            for (int i = 0; i < totalEventsExpected; i++) {
                Event event = inputMailbox.take();
                int value = random.nextInt(201);
                processedCount++;

                if (value % 8 == 0) {
                    suspiciousCount++;
                    alertMailbox.put(new Alert(event, value));
                    System.out.printf(
                        "[%s] %s -> sospechoso (valor=%d). Enviado a %s.%n",
                        getName(),
                        event.getEventId(),
                        value,
                        alertMailbox.getName()
                    );
                } else {
                    normalCount++;
                    // Espera semi-activa: el buzon de clasificacion es limitado por tam1.
                    classificationMailbox.putSemiActive(event);
                    System.out.printf(
                        "[%s] %s -> normal (valor=%d). Enviado a %s.%n",
                        getName(),
                        event.getEventId(),
                        value,
                        classificationMailbox.getName()
                    );
                }
            }

            alertMailbox.put(Alert.finishSignal());
            System.out.printf(
                "[%s] Fin. Procesados=%d, normales=%d, sospechosos=%d. Fin enviado al administrador.%n",
                getName(),
                processedCount,
                normalCount,
                suspiciousCount
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.printf("[%s] Interrumpido.%n", getName());
        }
    }

    public int getProcessedCount() {
        return processedCount;
    }

    public int getNormalCount() {
        return normalCount;
    }

    public int getSuspiciousCount() {
        return suspiciousCount;
    }
}

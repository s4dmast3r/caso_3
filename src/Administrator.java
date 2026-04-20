import java.util.concurrent.ThreadLocalRandom;

public class Administrator extends Thread {
    private final BlockingMailbox<Alert> alertQueue;
    private final EventQueue classifierQueue;
    private final int numClassifiers;

    public Administrator(
        BlockingMailbox<Alert> alertQueue,
        EventQueue classifierQueue,
        int numClassifiers
    ) {
        super("Administrator");
        this.alertQueue = alertQueue;
        this.classifierQueue = classifierQueue;
        this.numClassifiers = numClassifiers;
    }

    @Override
    public void run() {
        try {
            System.out.printf("[%s] Iniciando validacion de alertas.%n", getName());

            int validAlerts = 0;
            int discardedAlerts = 0;

            while (true) {
                Alert alert = alertQueue.take();
                if (alert.isShutdownSignal()) {
                    System.out.printf(
                        "[%s] Evento de fin recibido. Alertas validas=%d, descartadas=%d.%n",
                        getName(),
                        validAlerts,
                        discardedAlerts
                    );
                    for (int i = 0; i < numClassifiers; i++) {
                        classifierQueue.put(Event.shutdownSignal());
                    }
                    System.out.printf("[%s] Enviados %d eventos de fin a clasificadores.%n", getName(), numClassifiers);
                    break;
                }

                int randomValue = ThreadLocalRandom.current().nextInt(0, 21);
                if (randomValue % 4 == 0) {
                    validAlerts++;
                    classifierQueue.put(alert.getEvent());
                    System.out.printf(
                        "[%s] Alerta VALIDA (valor=%d) enviada a clasificacion: %s%n",
                        getName(),
                        randomValue,
                        alert.getEvent().getEventId()
                    );
                } else {
                    discardedAlerts++;
                    System.out.printf(
                        "[%s] Alerta DESCARTADA (valor=%d): %s%n",
                        getName(),
                        randomValue,
                        alert.getEvent().getEventId()
                    );
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.printf("[%s] Interrumpido.%n", getName());
        }
    }
}

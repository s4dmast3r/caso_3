import java.util.Random;

public class Administrator extends Thread {
    private final PassiveMailbox<Alert> alertMailbox;
    private final LimitedMailbox<Event> classificationMailbox;
    private final int numClassifiers;
    private final Random random;

    private int alertsReceived;
    private int harmlessCount;
    private int discardedCount;

    public Administrator(
        PassiveMailbox<Alert> alertMailbox,
        LimitedMailbox<Event> classificationMailbox,
        int numClassifiers
    ) {
        super("Administrator");
        this.alertMailbox = alertMailbox;
        this.classificationMailbox = classificationMailbox;
        this.numClassifiers = numClassifiers;
        this.random = new Random(System.nanoTime() + 31);
    }

    @Override
    public void run() {
        try {
            System.out.printf("[%s] Inicio. Esperando alertas en %s.%n", getName(), alertMailbox.getName());

            while (true) {
                Alert alert = alertMailbox.take();
                if (alert.isFinishSignal()) {
                    System.out.printf(
                        "[%s] Fin recibido del broker. Alertas=%d, inofensivas=%d, descartadas=%d.%n",
                        getName(),
                        alertsReceived,
                        harmlessCount,
                        discardedCount
                    );

                    for (int i = 1; i <= numClassifiers; i++) {
                        // Espera semi-activa: los eventos de fin tambien respetan tam1.
                        classificationMailbox.putSemiActive(Event.finishSignal("ADMIN_A_CLASSIFIER_" + i));
                    }

                    System.out.printf("[%s] Enviados %d eventos de fin a clasificadores.%n", getName(), numClassifiers);
                    break;
                }

                alertsReceived++;
                int value = random.nextInt(21);
                Event event = alert.getEvent();

                if (value % 4 == 0) {
                    harmlessCount++;
                    // Espera semi-activa: reenvio hacia buzon limitado de clasificacion.
                    classificationMailbox.putSemiActive(event);
                    System.out.printf(
                        "[%s] %s -> alerta inofensiva (valor=%d). Reenviada a %s.%n",
                        getName(),
                        event.getEventId(),
                        value,
                        classificationMailbox.getName()
                    );
                } else {
                    discardedCount++;
                    System.out.printf(
                        "[%s] %s -> alerta descartada (valor=%d).%n",
                        getName(),
                        event.getEventId(),
                        value
                    );
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.printf("[%s] Interrumpido.%n", getName());
        }
    }

    public int getAlertsReceived() {
        return alertsReceived;
    }

    public int getHarmlessCount() {
        return harmlessCount;
    }

    public int getDiscardedCount() {
        return discardedCount;
    }
}

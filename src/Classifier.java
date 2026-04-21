import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Classifier extends Thread {
    private final int classifierId;
    private final LimitedMailbox<Event> classificationMailbox;
    private final List<LimitedMailbox<Event>> serverMailboxes;
    private final CyclicBarrier terminationBarrier;
    private final ClassifierTerminationState terminationState;

    private int sentToServersCount;

    public Classifier(
        int classifierId,
        LimitedMailbox<Event> classificationMailbox,
        List<LimitedMailbox<Event>> serverMailboxes,
        CyclicBarrier terminationBarrier,
        ClassifierTerminationState terminationState
    ) {
        super("Classifier-" + classifierId);
        this.classifierId = classifierId;
        this.classificationMailbox = classificationMailbox;
        this.serverMailboxes = serverMailboxes;
        this.terminationBarrier = terminationBarrier;
        this.terminationState = terminationState;
    }

    @Override
    public void run() {
        try {
            System.out.printf("[%s] Inicio. Leyendo de %s.%n", getName(), classificationMailbox.getName());

            while (true) {
                // Espera semi-activa: el buzon de clasificacion es limitado.
                Event event = classificationMailbox.takeSemiActive();

                if (event.isFinishSignal()) {
                    handleFinishSignal(event);
                    break;
                }

                int targetServerId = event.getTargetServerId();
                LimitedMailbox<Event> targetMailbox = serverMailboxes.get(targetServerId - 1);
                String typeLabel = identifyType(event);

                // Espera semi-activa: cada buzon de consolidacion esta limitado por tam2.
                targetMailbox.putSemiActive(event);
                sentToServersCount++;

                System.out.printf(
                    "[%s] %s identificado como %s. Enviado a servidor %d (%s).%n",
                    getName(),
                    event.getEventId(),
                    typeLabel,
                    targetServerId,
                    targetMailbox.getName()
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.printf("[%s] Interrumpido.%n", getName());
        }
    }

    private String identifyType(Event event) {
        return "TYPE_" + event.getEventType();
    }

    private void handleFinishSignal(Event event) throws InterruptedException {
        boolean lastClassifier = terminationState.markTerminatedAndIsLast(classifierId);
        System.out.printf("[%s] Evento de fin recibido (%s). Esperando barrera.%n", getName(), event.getFinishSource());

        try {
            terminationBarrier.await();
        } catch (BrokenBarrierException e) {
            InterruptedException interrupted = new InterruptedException("Barrera de clasificadores rota.");
            interrupted.initCause(e);
            throw interrupted;
        }

        if (lastClassifier) {
            System.out.printf("[%s] Ultimo clasificador. Enviando fin a %d servidores.%n", getName(), serverMailboxes.size());
            for (int i = 0; i < serverMailboxes.size(); i++) {
                LimitedMailbox<Event> serverMailbox = serverMailboxes.get(i);
                serverMailbox.putSemiActive(Event.finishSignal("CLASSIFIERS_A_SERVER_" + (i + 1)));
            }
        }

        System.out.printf("[%s] Fin. Eventos enviados a servidores=%d.%n", getName(), sentToServersCount);
    }

    public int getSentToServersCount() {
        return sentToServersCount;
    }
}

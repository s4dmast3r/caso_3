import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class Classifier extends Thread {
    private final int classifierId;
    private final EventQueue inputQueue;
    private final Map<Integer, BlockingMailbox<ClassifiedEvent>> consolidationQueues;
    private final CyclicBarrier terminationBarrier;
    private final AtomicInteger classifiersTerminated;
    private final int totalClassifiers;
    private final int numServers;

    public Classifier(
        int classifierId,
        EventQueue inputQueue,
        Map<Integer, BlockingMailbox<ClassifiedEvent>> consolidationQueues,
        CyclicBarrier terminationBarrier,
        AtomicInteger classifiersTerminated,
        int totalClassifiers,
        int numServers
    ) {
        super("Classifier-" + classifierId);
        this.classifierId = classifierId;
        this.inputQueue = inputQueue;
        this.consolidationQueues = consolidationQueues;
        this.terminationBarrier = terminationBarrier;
        this.classifiersTerminated = classifiersTerminated;
        this.totalClassifiers = totalClassifiers;
        this.numServers = numServers;
    }

    @Override
    public void run() {
        try {
            System.out.printf("[%s] Iniciando clasificacion.%n", getName());

            while (true) {
                Event event = inputQueue.take();

                if (event.isShutdownSignal()) {
                    System.out.printf("[%s] Evento de fin recibido.%n", getName());
                    try {
                        terminationBarrier.await();
                        int terminated = classifiersTerminated.incrementAndGet();
                        System.out.printf("[%s] Paso barrera. Terminados=%d.%n", getName(), terminated);

                        if (terminated == totalClassifiers) {
                            System.out.printf("[%s] Soy el ultimo. Enviando fin a %d servidores.%n", getName(), numServers);
                            for (int i = 1; i <= numServers; i++) {
                                consolidationQueues.get(i).put(ClassifiedEvent.shutdownSignal());
                            }
                        }
                    } catch (BrokenBarrierException e) {
                        Thread.currentThread().interrupt();
                    }
                    break;
                }

                ClassifiedEvent classifiedEvent = classifyEvent(event);
                int serverId = event.getTargetServerId();
                consolidationQueues.get(serverId).put(classifiedEvent);
                System.out.printf("[%s] Evento clasificado enviado a servidor %d: %s%n", getName(), serverId, classifiedEvent);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.printf("[%s] Interrumpido.%n", getName());
        }
    }

    private ClassifiedEvent classifyEvent(Event event) {
        String category;
        String destination;

        switch (event.getType()) {
            case TEMPERATURE:
            case HUMIDITY:
            case PRESSURE:
                category = "ENVIRONMENTAL";
                destination = "CONSOLIDATION_CLUSTER";
                break;
            case MOTION:
                category = "SECURITY";
                destination = "REAL_TIME_DASHBOARD";
                break;
            case SMOKE:
                category = "SAFETY";
                destination = "EMERGENCY_DASHBOARD";
                break;
            default:
                category = "UNKNOWN";
                destination = "BACKLOG";
                break;
        }

        return new ClassifiedEvent(event, category, destination);
    }
}

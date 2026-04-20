import java.util.concurrent.ThreadLocalRandom;

public class ConsolidationServer extends Thread {
    private final int serverId;
    private final BlockingMailbox<ClassifiedEvent> inputQueue;
    private final BlockingMailbox<ConsolidatedUpdate> outputQueue;

    public ConsolidationServer(
        int serverId,
        BlockingMailbox<ClassifiedEvent> inputQueue,
        BlockingMailbox<ConsolidatedUpdate> outputQueue
    ) {
        super("ConsolidationServer-" + serverId);
        this.serverId = serverId;
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {
        try {
            System.out.printf("[%s] Iniciado.%n", getName());

            while (true) {
                ClassifiedEvent event = inputQueue.take();

                if (event.isShutdownSignal()) {
                    System.out.printf("[%s] Evento de fin recibido.%n", getName());
                    outputQueue.put(ConsolidatedUpdate.shutdownSignal());
                    break;
                }

                int processingTime = ThreadLocalRandom.current().nextInt(100, 1001);
                System.out.printf(
                    "[%s] Procesando evento %s durante %dms.%n",
                    getName(),
                    event.getEvent().getEventId(),
                    processingTime
                );
                Thread.sleep(processingTime);

                ConsolidatedUpdate update = new ConsolidatedUpdate(
                    event.getEvent(),
                    "Consolidado en servidor " + serverId
                );
                outputQueue.put(update);
                System.out.printf("[%s] Evento consolidado y enviado a despliegue.%n", getName());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.printf("[%s] Interrumpido.%n", getName());
        }
    }
}

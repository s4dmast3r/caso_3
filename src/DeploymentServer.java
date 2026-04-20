public class DeploymentServer extends Thread {
    private final BlockingMailbox<ConsolidatedUpdate> inputQueue;
    private final int expectedShutdownSignals;

    public DeploymentServer(BlockingMailbox<ConsolidatedUpdate> inputQueue, int expectedShutdownSignals) {
        super("DeploymentServer");
        this.inputQueue = inputQueue;
        this.expectedShutdownSignals = expectedShutdownSignals;
    }

    @Override
    public void run() {
        try {
            System.out.printf("[%s] Iniciado.%n", getName());

            int shutdownSignalsReceived = 0;
            while (true) {
                ConsolidatedUpdate update = inputQueue.take();
                if (update.isShutdownSignal()) {
                    shutdownSignalsReceived++;
                    System.out.printf(
                        "[%s] Evento de fin recibido (%d/%d).%n",
                        getName(),
                        shutdownSignalsReceived,
                        expectedShutdownSignals
                    );
                    if (shutdownSignalsReceived >= expectedShutdownSignals) {
                        System.out.printf("[%s] Finalizando despliegue.%n", getName());
                        break;
                    }
                    continue;
                }

                System.out.printf(
                    "[%s] EVENTO DESPLEGADO: %s (Estado: %s)%n",
                    getName(),
                    update.getEvent().getEventId(),
                    update.getStatus()
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.printf("[%s] Interrumpido.%n", getName());
        }
    }
}

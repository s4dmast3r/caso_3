import java.util.Random;

public class ServerNode extends Thread {
    private final int serverId;
    private final LimitedMailbox<Event> consolidationMailbox;
    private final Random random;
    private int processedCount;

    public ServerNode(int serverId, LimitedMailbox<Event> consolidationMailbox) {
        super("ServerNode-" + serverId);
        this.serverId = serverId;
        this.consolidationMailbox = consolidationMailbox;
        this.random = new Random(System.nanoTime() + serverId * 17L);
    }

    @Override
    public void run() {
        try {
            System.out.printf("[%s] Inicio. Buzon propio=%s.%n", getName(), consolidationMailbox.getName());

            while (true) {
                // Espera semi-activa: cada servidor consume de su buzon limitado.
                Event event = consolidationMailbox.takeSemiActive();

                if (event.isFinishSignal()) {
                    System.out.printf("[%s] Evento de fin recibido (%s).%n", getName(), event.getFinishSource());
                    break;
                }

                int processingTimeMs = 100 + random.nextInt(901);
                Thread.sleep(processingTimeMs);
                processedCount++;

                System.out.printf(
                    "[%s] DESPLIEGUE: %s consolidado como %s en %d ms.%n",
                    getName(),
                    event.getEventId(),
                    event.getTypeLabel(),
                    processingTimeMs
                );
            }

            System.out.printf("[%s] Fin. Eventos procesados=%d.%n", getName(), processedCount);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.printf("[%s] Interrumpido.%n", getName());
        }
    }

    public int getProcessedCount() {
        return processedCount;
    }
}

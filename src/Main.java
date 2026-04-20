import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
        try {
            String configFile = args.length > 0 ? args[0] : "config.txt";

            System.out.println("==============================================");
            System.out.println(" SISTEMA IOT - CONCURRENCIA Y SINCRONIZACION ");
            System.out.println("==============================================");

            ConfigReader config = new ConfigReader(configFile);
            config.validate();
            config.printConfig();

            int numSensores = config.getInt("numSensores");
            int numEventosBase = config.getInt("numEventosBase");
            int numClasificadores = config.getInt("numClasificadores");
            int numServidores = config.getInt("numServidores");
            int capacidadClasificacion = config.getInt("capacidadClasificacion");
            int capacidadConsolidacion = config.getInt("capacidadConsolidacion");

            int totalEvents = 0;
            for (int i = 1; i <= numSensores; i++) {
                totalEvents += numEventosBase * i;
            }
            System.out.printf("Total de eventos esperados: %d%n%n", totalEvents);

            EventQueue brokerInbox = new EventQueue("BuzonEntrada", Integer.MAX_VALUE);
            BlockingMailbox<Alert> administratorInbox =
                new BlockingMailbox<>("BuzonAlertas", capacidadClasificacion);
            EventQueue classifierInbox =
                new EventQueue("BuzonClasificacion", capacidadClasificacion);

            Map<Integer, BlockingMailbox<ClassifiedEvent>> consolidationInboxes = new HashMap<>();
            for (int i = 1; i <= numServidores; i++) {
                consolidationInboxes.put(
                    i,
                    new BlockingMailbox<>("BuzonConsolidacion-" + i, capacidadConsolidacion)
                );
            }

            BlockingMailbox<ConsolidatedUpdate> deploymentInbox =
                new BlockingMailbox<>("BuzonDespliegue", Integer.MAX_VALUE);

            CyclicBarrier classifierBarrier = new CyclicBarrier(numClasificadores);
            AtomicInteger classifiersTerminated = new AtomicInteger(0);

            List<Sensor> sensors = new ArrayList<>();
            for (int i = 1; i <= numSensores; i++) {
                int eventsForThisSensor = numEventosBase * i;
                sensors.add(new Sensor(i, brokerInbox, eventsForThisSensor, numServidores));
            }

            Broker broker = new Broker(
                brokerInbox,
                administratorInbox,
                classifierInbox,
                totalEvents
            );

            Administrator administrator = new Administrator(
                administratorInbox,
                classifierInbox,
                numClasificadores
            );

            List<Classifier> classifiers = new ArrayList<>();
            for (int i = 1; i <= numClasificadores; i++) {
                classifiers.add(
                    new Classifier(
                        i,
                        classifierInbox,
                        consolidationInboxes,
                        classifierBarrier,
                        classifiersTerminated,
                        numClasificadores,
                        numServidores
                    )
                );
            }

            List<ConsolidationServer> consolidationServers = new ArrayList<>();
            for (int i = 1; i <= numServidores; i++) {
                consolidationServers.add(
                    new ConsolidationServer(
                        i,
                        consolidationInboxes.get(i),
                        deploymentInbox
                    )
                );
            }

            DeploymentServer deploymentServer = new DeploymentServer(deploymentInbox, numServidores);

            deploymentServer.start();
            for (ConsolidationServer server : consolidationServers) {
                server.start();
            }
            administrator.start();
            broker.start();
            for (Classifier classifier : classifiers) {
                classifier.start();
            }
            for (Sensor sensor : sensors) {
                sensor.start();
            }

            for (Sensor sensor : sensors) {
                sensor.join();
            }
            broker.join();
            administrator.join();
            for (Classifier classifier : classifiers) {
                classifier.join();
            }
            for (ConsolidationServer server : consolidationServers) {
                server.join();
            }
            deploymentServer.join();

            System.out.println();
            System.out.println("Sistema IoT finalizado correctamente.");
        } catch (IOException e) {
            System.err.printf("No se pudo leer el archivo de configuracion: %s%n", e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.printf("Configuracion invalida: %s%n", e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("La ejecucion del sistema fue interrumpida.");
            System.exit(1);
        }
    }
}

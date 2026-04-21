import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class Main {
    public static void main(String[] args) {
        try {
            String configFile = args.length > 0 ? args[0] : "config.txt";

            System.out.println("==============================================");
            System.out.println(" CASO 3 - SISTEMA IOT CONCURRENTE ");
            System.out.println("==============================================");
            System.out.println("Inicio del sistema.");

            ConfigReader config = new ConfigReader(configFile);
            config.validate();
            config.printConfig();

            int ni = config.getNi();
            int baseEventos = config.getBaseEventos();
            int nc = config.getNc();
            int ns = config.getNs();
            int tam1 = config.getTam1();
            int tam2 = config.getTam2();
            int totalEventsExpected = calculateTotalEventsExpected(ni, baseEventos);

            System.out.printf("Total de eventos esperados: %d%n%n", totalEventsExpected);

            PassiveMailbox<Event> inputMailbox = new PassiveMailbox<>("BuzonEntradaIlimitado");
            PassiveMailbox<Alert> alertMailbox = new PassiveMailbox<>("BuzonAlertasIlimitado");
            LimitedMailbox<Event> classificationMailbox = new LimitedMailbox<>("BuzonClasificacionLimitado", tam1);

            List<LimitedMailbox<Event>> serverMailboxes = new ArrayList<>();
            for (int i = 1; i <= ns; i++) {
                serverMailboxes.add(new LimitedMailbox<>("BuzonConsolidacionServidor-" + i, tam2));
            }

            List<Sensor> sensors = new ArrayList<>();
            for (int sensorId = 1; sensorId <= ni; sensorId++) {
                int eventsForSensor = baseEventos * sensorId;
                sensors.add(new Sensor(sensorId, eventsForSensor, ns, inputMailbox));
            }

            Broker broker = new Broker(inputMailbox, alertMailbox, classificationMailbox, totalEventsExpected);
            Administrator administrator = new Administrator(alertMailbox, classificationMailbox, nc);

            CyclicBarrier classifierBarrier = new CyclicBarrier(nc);
            ClassifierTerminationState terminationState = new ClassifierTerminationState(nc);
            List<Classifier> classifiers = new ArrayList<>();
            for (int classifierId = 1; classifierId <= nc; classifierId++) {
                classifiers.add(
                    new Classifier(
                        classifierId,
                        classificationMailbox,
                        serverMailboxes,
                        classifierBarrier,
                        terminationState
                    )
                );
            }

            List<ServerNode> servers = new ArrayList<>();
            for (int serverId = 1; serverId <= ns; serverId++) {
                servers.add(new ServerNode(serverId, serverMailboxes.get(serverId - 1)));
            }

            startAll(servers, classifiers, administrator, broker, sensors);
            joinAll(sensors, broker, administrator, classifiers, servers);

            boolean countsOk = validateCounts(
                totalEventsExpected,
                sensors,
                broker,
                administrator,
                classifiers,
                servers,
                terminationState,
                nc
            );
            boolean mailboxesOk = validateMailboxes(inputMailbox, alertMailbox, classificationMailbox, serverMailboxes);

            if (countsOk && mailboxesOk) {
                System.out.println("Resultado final: EXITO. Sistema finalizado correctamente y buzones vacios.");
            } else {
                System.err.println("Resultado final: FALLO. Revisar conteos o buzones no vacios.");
                System.exit(2);
            }
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

    private static int calculateTotalEventsExpected(int ni, int baseEventos) {
        long total = 0;
        for (int i = 1; i <= ni; i++) {
            total += (long) baseEventos * i;
        }

        if (total > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("El total de eventos excede el maximo soportado por int.");
        }

        return (int) total;
    }

    private static void startAll(
        List<ServerNode> servers,
        List<Classifier> classifiers,
        Administrator administrator,
        Broker broker,
        List<Sensor> sensors
    ) {
        for (ServerNode server : servers) {
            server.start();
        }
        for (Classifier classifier : classifiers) {
            classifier.start();
        }
        administrator.start();
        broker.start();
        for (Sensor sensor : sensors) {
            sensor.start();
        }
    }

    private static void joinAll(
        List<Sensor> sensors,
        Broker broker,
        Administrator administrator,
        List<Classifier> classifiers,
        List<ServerNode> servers
    ) throws InterruptedException {
        for (Sensor sensor : sensors) {
            sensor.join();
        }
        broker.join();
        administrator.join();
        for (Classifier classifier : classifiers) {
            classifier.join();
        }
        for (ServerNode server : servers) {
            server.join();
        }
    }

    private static boolean validateCounts(
        int totalEventsExpected,
        List<Sensor> sensors,
        Broker broker,
        Administrator administrator,
        List<Classifier> classifiers,
        List<ServerNode> servers,
        ClassifierTerminationState terminationState,
        int expectedClassifiersTerminated
    ) {
        int generatedBySensors = 0;
        for (Sensor sensor : sensors) {
            generatedBySensors += sensor.getGeneratedCount();
        }

        int sentByClassifiers = 0;
        for (Classifier classifier : classifiers) {
            sentByClassifiers += classifier.getSentToServersCount();
        }

        int processedByServers = 0;
        for (ServerNode server : servers) {
            processedByServers += server.getProcessedCount();
        }

        int brokerProcessed = broker.getProcessedCount();
        int brokerNormal = broker.getNormalCount();
        int brokerSuspicious = broker.getSuspiciousCount();
        int adminResolved = administrator.getHarmlessCount() + administrator.getDiscardedCount();
        int expectedClassifiedEvents = brokerNormal + administrator.getHarmlessCount();

        System.out.println();
        System.out.println("Validacion de conteos:");
        boolean ok = true;
        ok &= check("Eventos esperados vs generados por sensores", totalEventsExpected, generatedBySensors);
        ok &= check("Eventos esperados vs procesados por broker", totalEventsExpected, brokerProcessed);
        ok &= check("Normales + sospechosos del broker", totalEventsExpected, brokerNormal + brokerSuspicious);
        ok &= check("Alertas del broker vs recibidas por administrador", brokerSuspicious, administrator.getAlertsReceived());
        ok &= check("Alertas recibidas vs resueltas por administrador", administrator.getAlertsReceived(), adminResolved);
        ok &= check("Eventos clasificados esperados vs enviados a servidores", expectedClassifiedEvents, sentByClassifiers);
        ok &= check("Eventos enviados a servidores vs procesados por servidores", sentByClassifiers, processedByServers);
        ok &= check(
            "Clasificadores terminados",
            expectedClassifiersTerminated,
            terminationState.getTerminatedClassifiers()
        );

        System.out.printf(
            "Resumen: broker normales=%d, broker sospechosos=%d, admin descartadas=%d, admin reenviadas=%d, servidores procesaron=%d.%n",
            brokerNormal,
            brokerSuspicious,
            administrator.getDiscardedCount(),
            administrator.getHarmlessCount(),
            processedByServers
        );

        return ok;
    }

    private static boolean validateMailboxes(
        PassiveMailbox<Event> inputMailbox,
        PassiveMailbox<Alert> alertMailbox,
        LimitedMailbox<Event> classificationMailbox,
        List<LimitedMailbox<Event>> serverMailboxes
    ) {
        System.out.println();
        System.out.println("Verificacion final de buzones:");
        boolean ok = true;
        ok &= checkMailbox(inputMailbox.getName(), inputMailbox.size());
        ok &= checkMailbox(alertMailbox.getName(), alertMailbox.size());
        ok &= checkMailbox(classificationMailbox.getName(), classificationMailbox.size());

        for (LimitedMailbox<Event> serverMailbox : serverMailboxes) {
            ok &= checkMailbox(serverMailbox.getName(), serverMailbox.size());
        }

        return ok;
    }

    private static boolean check(String label, int expected, int actual) {
        if (expected == actual) {
            System.out.printf("  OK - %s: %d%n", label, actual);
            return true;
        }

        System.err.printf("  ERROR - %s: esperado=%d, actual=%d%n", label, expected, actual);
        return false;
    }

    private static boolean checkMailbox(String name, int size) {
        if (size == 0) {
            System.out.printf("  OK - %s vacio.%n", name);
            return true;
        }

        System.err.printf("  ERROR - %s no esta vacio. Elementos restantes=%d%n", name, size);
        return false;
    }
}

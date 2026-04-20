import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private final Properties properties;

    public ConfigReader(String filename) throws IOException {
        properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(filename)) {
            properties.load(inputStream);
        }
    }

    public int getInt(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Falta parametro: " + key);
        }
        return Integer.parseInt(value.trim());
    }

    public int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        return value == null ? defaultValue : Integer.parseInt(value.trim());
    }

    public void validate() {
        String[] requiredKeys = {
            "numSensores",
            "numEventosBase",
            "numClasificadores",
            "numServidores",
            "capacidadClasificacion",
            "capacidadConsolidacion"
        };

        for (String key : requiredKeys) {
            if (!properties.containsKey(key)) {
                throw new IllegalArgumentException("Falta parametro obligatorio: " + key);
            }

            int value = getInt(key);
            if (value <= 0) {
                throw new IllegalArgumentException("El parametro " + key + " debe ser mayor que cero.");
            }
        }
    }

    public void printConfig() {
        System.out.println("Configuracion cargada:");
        System.out.printf("  Sensores: %d%n", getInt("numSensores"));
        System.out.printf("  Eventos base: %d%n", getInt("numEventosBase"));
        System.out.printf("  Clasificadores: %d%n", getInt("numClasificadores"));
        System.out.printf("  Servidores: %d%n", getInt("numServidores"));
        System.out.printf("  Capacidad clasificacion: %d%n", getInt("capacidadClasificacion"));
        System.out.printf("  Capacidad consolidacion: %d%n%n", getInt("capacidadConsolidacion"));
    }
}

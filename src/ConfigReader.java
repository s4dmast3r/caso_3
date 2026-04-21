import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private final Properties properties = new Properties();
    private boolean validated;

    private int ni;
    private int baseEventos;
    private int nc;
    private int ns;
    private int tam1;
    private int tam2;

    public ConfigReader(String filename) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(filename)) {
            properties.load(inputStream);
        }
    }

    public void validate() {
        if (validated) {
            return;
        }

        ni = readPositive("ni", "numSensores");
        baseEventos = readPositive("base_eventos", "numEventosBase");
        nc = readPositive("nc", "numClasificadores");
        ns = readPositive("ns", "numServidores");
        tam1 = readPositive("tam1", "capacidadClasificacion");
        tam2 = readPositive("tam2", "capacidadConsolidacion");
        validated = true;
    }

    public int getInt(String key) {
        ensureValidated();

        if ("ni".equals(key) || "numSensores".equals(key)) {
            return ni;
        }
        if ("base_eventos".equals(key) || "numEventosBase".equals(key)) {
            return baseEventos;
        }
        if ("nc".equals(key) || "numClasificadores".equals(key)) {
            return nc;
        }
        if ("ns".equals(key) || "numServidores".equals(key)) {
            return ns;
        }
        if ("tam1".equals(key) || "capacidadClasificacion".equals(key)) {
            return tam1;
        }
        if ("tam2".equals(key) || "capacidadConsolidacion".equals(key)) {
            return tam2;
        }

        throw new IllegalArgumentException("Parametro no reconocido: " + key);
    }

    public int getNi() {
        ensureValidated();
        return ni;
    }

    public int getBaseEventos() {
        ensureValidated();
        return baseEventos;
    }

    public int getNc() {
        ensureValidated();
        return nc;
    }

    public int getNs() {
        ensureValidated();
        return ns;
    }

    public int getTam1() {
        ensureValidated();
        return tam1;
    }

    public int getTam2() {
        ensureValidated();
        return tam2;
    }

    public void printConfig() {
        ensureValidated();

        System.out.println("Configuracion cargada:");
        System.out.printf("  ni / numSensores: %d%n", ni);
        System.out.printf("  base_eventos / numEventosBase: %d%n", baseEventos);
        System.out.printf("  nc / numClasificadores: %d%n", nc);
        System.out.printf("  ns / numServidores: %d%n", ns);
        System.out.printf("  tam1 / capacidadClasificacion: %d%n", tam1);
        System.out.printf("  tam2 / capacidadConsolidacion: %d%n", tam2);
        System.out.println();
    }

    private void ensureValidated() {
        if (!validated) {
            validate();
        }
    }

    private int readPositive(String statementKey, String legacyKey) {
        String statementValue = normalize(properties.getProperty(statementKey));
        String legacyValue = normalize(properties.getProperty(legacyKey));

        if (statementValue == null && legacyValue == null) {
            throw new IllegalArgumentException(
                "Falta parametro obligatorio: " + statementKey + " o " + legacyKey
            );
        }

        String selectedKey = statementValue != null ? statementKey : legacyKey;
        String selectedValue = statementValue != null ? statementValue : legacyValue;

        if (statementValue != null && legacyValue != null && !statementValue.equals(legacyValue)) {
            System.out.printf(
                "Advertencia: %s y %s tienen valores distintos. Se usara %s=%s.%n",
                statementKey,
                legacyKey,
                selectedKey,
                selectedValue
            );
        }

        int value;
        try {
            value = Integer.parseInt(selectedValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El parametro " + selectedKey + " debe ser entero.", e);
        }

        if (value <= 0) {
            throw new IllegalArgumentException("El parametro " + selectedKey + " debe ser mayor que cero.");
        }

        return value;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

# Ejecucion del Caso 3

Este proyecto no usa dependencias externas. Compila y ejecuta con `javac` y `java`.

## Compilar

Desde la carpeta raiz del proyecto:

```bash
javac src/*.java
```

## Ejecutar

Con el archivo de configuracion incluido:

```bash
java -cp src Main config.txt
```

Si no se pasa ruta de configuracion, `Main` intenta leer `config.txt`:

```bash
java -cp src Main
```

## Formato de configuracion

El lector acepta las claves del enunciado:

```properties
ni=3
base_eventos=8
nc=2
ns=3
tam1=10
tam2=10
```

Tambien acepta las claves antiguas:

```properties
numSensores=3
numEventosBase=8
numClasificadores=2
numServidores=3
capacidadClasificacion=10
capacidadConsolidacion=10
```

Todos los valores deben ser enteros mayores que cero.

## Flujo implementado

Sensores -> Broker/Analizador -> Administrador o Clasificadores -> ServerNode.

Cada `ServerNode` tiene su propio buzon limitado de consolidacion y despliega por consola los eventos que le corresponden.

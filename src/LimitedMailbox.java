import java.util.LinkedList;
import java.util.Queue;

public class LimitedMailbox<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final String name;
    private final int capacity;

    public LimitedMailbox(String name, int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("La capacidad de " + name + " debe ser mayor que cero.");
        }
        this.name = name;
        this.capacity = capacity;
    }

    public synchronized boolean tryPut(T item) {
        if (item == null) {
            throw new IllegalArgumentException("No se permiten elementos null en " + name);
        }

        if (queue.size() >= capacity) {
            return false;
        }

        queue.add(item);
        return true;
    }

    public synchronized T tryTake() {
        if (queue.isEmpty()) {
            return null;
        }
        return queue.poll();
    }

    public void putSemiActive(T item) throws InterruptedException {
        // Espera semi-activa: no se llama wait; se reintenta con yield hasta que haya cupo.
        while (!tryPut(item)) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Interrumpido insertando en " + name);
            }
            Thread.yield();
        }
    }

    public T takeSemiActive() throws InterruptedException {
        // Espera semi-activa: no se llama wait; se reintenta con yield hasta que haya datos.
        T item = tryTake();
        while (item == null) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Interrumpido retirando de " + name);
            }
            Thread.yield();
            item = tryTake();
        }
        return item;
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }
}

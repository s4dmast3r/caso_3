import java.util.LinkedList;
import java.util.Queue;

public class PassiveMailbox<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final String name;

    public PassiveMailbox(String name) {
        this.name = name;
    }

    public synchronized void put(T item) {
        if (item == null) {
            throw new IllegalArgumentException("No se permiten elementos null en " + name);
        }

        queue.add(item);
        notifyAll();
    }

    public synchronized T take() throws InterruptedException {
        // Espera pasiva: wait libera el monitor hasta que alguien inserta y notifica.
        while (queue.isEmpty()) {
            wait();
        }

        T item = queue.poll();
        notifyAll();
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
}

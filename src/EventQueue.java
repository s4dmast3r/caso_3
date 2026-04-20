public class EventQueue extends BlockingMailbox<Event> {
    public EventQueue(String name, int capacity) {
        super(name, capacity);
    }
}

public class ClassifierTerminationState {
    private final int totalClassifiers;
    private int terminatedClassifiers;
    private int lastClassifierId;

    public ClassifierTerminationState(int totalClassifiers) {
        this.totalClassifiers = totalClassifiers;
    }

    public synchronized boolean markTerminatedAndIsLast(int classifierId) {
        terminatedClassifiers++;
        if (terminatedClassifiers == totalClassifiers) {
            lastClassifierId = classifierId;
            return true;
        }
        return false;
    }

    public synchronized int getTerminatedClassifiers() {
        return terminatedClassifiers;
    }

    public synchronized int getLastClassifierId() {
        return lastClassifierId;
    }
}

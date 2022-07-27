```
看一下acquire和release
public abstract class AbstractQueuedSynchronizer
    extends AbstractOwnableSynchronizer
    implements java.io.Serializable {
    private volatile int state;
    public final void acquire(int arg) {
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }
    public final boolean release(int arg) {
        if (tryRelease(arg)) {
            Node h = head;
            if (h != null && h.waitStatus != 0)
                unparkSuccessor(h);
            return true;
        }
        return false;
    }
    protected boolean tryAcquire(int arg) {
        throw new UnsupportedOperationException();
    }
    protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException();
    }
}
```


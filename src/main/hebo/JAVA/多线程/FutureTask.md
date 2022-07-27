FutureTask用于在异步操作场景中，FutureTask作为生产者(执行FutureTask的线程)和消费者(获取FutureTask结果的线程)的桥梁，如果生产者先生产出了数据，那么消费者get时能会直接拿到结果；如果生产者还未产生数据，那么get时会一直阻塞或者超时阻塞，一直到生产者产生数据唤醒阻塞的消费者为止。

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9qT0QzUFVVeFRpYk5mTWNsNmZZTmNaSWhXWUJtRzRRNW1pYXczNWx5MWljQWQyaWJodVBoa0duNmw1Qzc0R0FlTTVpYVJiNjR1bTJZS2hrUGNXNExQNE5admh3LzY0MA?x-oss-process=image/format,png)

当FutureTask处于未启动或已启动状态时，执行FutureTask.get()方法将导致调用线程阻塞；当FutureTask处于已完成状态时，执行FutureTask.get()方法将导致调用线程立即返回结果或抛出异常。

当FutureTask处于未启动状态时，执行FutureTask.cancel()方法将导致此任务永远不会被执行；

当FutureTask处于已启动状态时，执行FutureTask.cancel（true）方法将以中断执行此任务线程的方式来试图停止任务；

当FutureTask处于已启动状态时，执行FutureTask.cancel（false）方法将不会对正在执行此任务的线程产生影响（让正在执行的任务运行完成）；

当FutureTask处于已完成状态时，执行FutureTask.cancel（…）方法将返回false。





FutureTask中有一个int型的状态标志，表示future对应线程的运行状态。

```
/**
 * Possible state transitions:
 * NEW -> COMPLETING -> NORMAL
 * NEW -> COMPLETING -> EXCEPTIONAL
 * NEW -> CANCELLED
 * NEW -> INTERRUPTING -> INTERRUPTED
 */
private volatile int state;
private static final int NEW          = 0;
private static final int COMPLETING   = 1;
private static final int NORMAL       = 2;
private static final int EXCEPTIONAL  = 3;
private static final int CANCELLED    = 4;
private static final int INTERRUPTING = 5;
private static final int INTERRUPTED  = 6;
//cas+volatile+ 循环
 private volatile WaitNode waiters;
public V get() throws InterruptedException, ExecutionException {
    int s = state;
    if (s <= COMPLETING) // 线程未执行完成
        s = awaitDone(false, 0L);
    return report(s);
}
 
private int awaitDone(boolean timed, long nanos)
    throws InterruptedException {
    final long deadline = timed ? System.nanoTime() + nanos : 0L;
    WaitNode q = null;
    boolean queued = false;
    for (;;) {
        if (Thread.interrupted()) {
            removeWaiter(q);
            throw new InterruptedException();
        }
 
        int s = state;
        if (s > COMPLETING) { // 线程已运行完成
            if (q != null)
                q.thread = null;
            return s;
        }
        else if (s == COMPLETING) // cannot time out yet
            Thread.yield(); // future task已完成，正在赋值outcome，get()返回的值就是outcome，这时不用加入WaitNode即可
        else if (q == null)
            q = new WaitNode(); // 生成WaitNode
        else if (!queued)
            queued = UNSAFE.compareAndSwapObject(this, waitersOffset,
                                                 q.next = waiters, q);
        else if (timed) {
            nanos = deadline - System.nanoTime();
            if (nanos <= 0L) {
                removeWaiter(q);
                return state;
            }
            LockSupport.parkNanos(this, nanos);
        }
        else
            LockSupport.park(this);
    }
}
 
private V report(int s) throws ExecutionException {
    Object x = outcome;
    if (s == NORMAL) // 正常执行结束
        return (V)x;
    if (s >= CANCELLED) // 已取消
        throw new CancellationException();
    throw new ExecutionException((Throwable)x); // 抛出异常
}
```

在任务执行（run()方法）中，调用result = callable.call方法，正常执行完毕后调用set(result)设置Future结果；出现异常则调用setException(ex)。最后会调用finishCompletion()来唤醒阻塞在Future的所有线程。

设置完数据之后（不管是正常数据还是对应异常），当等待数据的线程来get时，就会返回或者直接给它抛异常；如果当线程已经get过并阻塞在这里时，FutureTask需要将这些线程唤醒起来。

```
public void run() {
    if (state != NEW ||
        !UNSAFE.compareAndSwapObject(this, runnerOffset,
                                     null, Thread.currentThread()))
        return;
    try {
        Callable<V> c = callable;
        if (c != null && state == NEW) {
            V result;
            boolean ran;
            try {
                result = c.call();
                ran = true;
            } catch (Throwable ex) {
                result = null;
                ran = false;
                setException(ex);
            }
            if (ran)
                set(result);
        }
    } finally {
        // runner must be non-null until state is settled to
        // prevent concurrent calls to run()
        runner = null;
        // state must be re-read after nulling runner to prevent
        // leaked interrupts
        int s = state;
        if (s >= INTERRUPTING)
            handlePossibleCancellationInterrupt(s);
    }
}
 
protected void set(V v) {
    if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
        outcome = v;
        UNSAFE.putOrderedInt(this, stateOffset, NORMAL); // final state
        finishCompletion();
    }
}
protected void setException(Throwable t) {
    if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
        outcome = t;
        UNSAFE.putOrderedInt(this, stateOffset, EXCEPTIONAL); // final state
        finishCompletion();
    }
}
 
// 唤醒所有等待线程
private void finishCompletion() {
    // assert state > COMPLETING;
    for (WaitNode q; (q = waiters) != null;) {
        if (UNSAFE.compareAndSwapObject(this, waitersOffset, q, null)) {
            for (;;) {
                Thread t = q.thread;
                if (t != null) {
                    q.thread = null;
                    LockSupport.unpark(t);
                }
                WaitNode next = q.next;
                if (next == null)
                    break;
                q.next = null; // unlink to help gc
                q = next;
            }
            break;
        }
    }
 
    done();
    callable = null;        // to reduce footprint
}
```

**FutureTask中的waiters是一个单向链表，如果多个线程阻塞在该Future上，最新阻塞的线程排列在链表前面，唤醒线程时依次从前到后遍历链表唤醒线程，这样处理貌似对最开始阻塞在Future上的线程不太公平哈，因为最开始阻塞的线程是到最后才被唤醒的**。